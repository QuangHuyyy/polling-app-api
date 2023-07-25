package com.example.api.service.impl;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.*;
import com.example.api.payload.request.*;
import com.example.api.payload.response.*;
import com.example.api.repository.*;
import com.example.api.service.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SemanticException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class PollServiceImpl implements IPollService {
    private final IPollRepository pollRepository;
    private final IFileDataService fileDataService;
    private final IVotingTypeService votingTypeService;
    private final ISettingSelectOptionService settingSelectOptionService;
    private final IUserService userService;
    private final IVoteRepository voteRepository;
    private final IVotingTokenRepository votingTokenRepository;

    @Override
    public Poll save(Poll poll) {
        return pollRepository.save(poll);
    }

    @Override
    @Transactional()
    public PagedResponse<PollResponse> getAllPollByUser(String userUuid, String filter, int page, int size, String[] sort) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sort[0].contains(",")) {
//            sort = ["field1,direction", "field2,direction"]
//            sort more than 2 fields
//            sortOrder = "field, direction"
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));

            }
        } else {
//            sort = [field, direction]
            orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        try {
            Page<Poll> pagePolls;
            if (filter.equals("participated")) {
                pagePolls = pollRepository.findAllByParticipated(userUuid, pageable);
            } else {
                pagePolls = pollRepository.findAllByUserUuid(userUuid, pageable);
            }

            List<Poll> polls = pagePolls.getContent();

            List<PollResponse> pollsResponse;
            if (pagePolls.getNumberOfElements() == 0) {
                pollsResponse = Collections.emptyList();
            } else {
                pollsResponse = new ArrayList<>();
                polls.forEach(p -> pollsResponse.add(convertToResponse(p)));
            }

            return new PagedResponse<>(pollsResponse, pagePolls.getNumber(), pagePolls.getSize(), pagePolls.getTotalElements(), pagePolls.getTotalPages(), pagePolls.getNumber(), pagePolls.isFirst(), pagePolls.isLast());
        } catch (SemanticException e) {
            throw new BadRequestException("Sort field invalid!");
        }
    }

    @Override
    public Poll getByUuid(String pollUuid/*, String userUuid*/) {
        return pollRepository.findById(pollUuid).orElseThrow(() -> new ResourceNotFoundException("Poll", "uuid", pollUuid));
    }

    @Override
    public void updateStatusByUuid(String uuid, EPollStatus status) {
        pollRepository.updateStatusByUuid(uuid, status);
    }

    @Override
    public Poll updatePoll(String uuid, Poll pollRequest, String thumbnailStatus, List<Long> imageAnswerIdsNoChange, List<String> labelsImageAnswerNoChange) {
        String filenameThumbnailOld = "";
        List<String> filenameImageDeleted = new ArrayList<>();

        Poll poll = getByUuid(uuid);

        poll.setTitle(pollRequest.getTitle());
        poll.setDescription(pollRequest.getDescription());

        switch (thumbnailStatus) {
            case "no_change" -> {
                if (poll.getThumbnail() != null) {
                    poll.setThumbnail(poll.getThumbnail());
                }
            }
            case "deleted" -> {
                if (poll.getThumbnail() != null) {
                    filenameThumbnailOld = poll.getThumbnail().getFilename();
                }

                poll.setThumbnail(null);
            }
            case "new" -> {
                if (poll.getThumbnail() != null) {
                    filenameThumbnailOld = poll.getThumbnail().getFilename();
                }
                poll.setThumbnail(pollRequest.getThumbnail());
            }
        }

        switch (pollRequest.getVotingType().getValue()) {
            case "multiple_choice" -> {
                if (!pollRequest.getMultipleChoiceAnswers().isEmpty()) {
                    List<Long> multipleChoiceIds = poll.getMultipleChoiceAnswers().stream().map(MultipleChoiceAnswer::getId).toList();
                    List<Long> multipleChoiceIdsRequest = pollRequest.getMultipleChoiceAnswers().stream().map(MultipleChoiceAnswer::getId).filter(Objects::nonNull).toList();

                    List<Long> multipleChoiceIdsDeleted = multipleChoiceIds.stream().filter(id -> !multipleChoiceIdsRequest.contains(id)).toList();
                    List<MultipleChoiceAnswer> multipleChoiceAnswers = poll.getMultipleChoiceAnswers();
                    multipleChoiceIdsDeleted.forEach(id -> multipleChoiceAnswers.removeIf(choice -> Objects.equals(choice.getId(), id)));

                    pollRequest.getMultipleChoiceAnswers().forEach(choice -> {
                        if (choice.getId() == null) {
                            MultipleChoiceAnswer choiceAnswer = MultipleChoiceAnswer.builder()
                                    .value(choice.getValue())
                                    .isOther(choice.isOther())
                                    .poll(poll)
                                    .build();

                            multipleChoiceAnswers.add(choiceAnswer);
                        }
                    });

                    poll.setMultipleChoiceAnswers(multipleChoiceAnswers);
                }
            }
            case "image" -> {
                List<Long> imageAnswerIds = poll.getImageAnswers().stream().map(ImageAnswer::getId).toList();
                List<Long> imageAnswerIdsDeleted = imageAnswerIds.stream().filter(id -> !imageAnswerIdsNoChange.contains(id)).toList();
                List<ImageAnswer> imageAnswers = poll.getImageAnswers();

                imageAnswerIdsDeleted.forEach(id -> {
                    ImageAnswer imageDeleted = imageAnswers.stream().filter(choice -> Objects.equals(choice.getId(), id)).findFirst().orElse(null);
                    if (imageDeleted != null) {
                        imageAnswers.remove(imageDeleted);
                        filenameImageDeleted.add(imageDeleted.getImage().getFilename());
                    }
                });

                for (int i = 0; i < imageAnswers.size(); i++) {
                    imageAnswers.get(i).setLabel(labelsImageAnswerNoChange.get(i));
                }

                if (!pollRequest.getImageAnswers().isEmpty()) {
                    pollRequest.getImageAnswers().forEach(img -> imageAnswers.add(
                            ImageAnswer.builder()
                                    .image(img.getImage())
                                    .label(img.getLabel())
                                    .poll(poll)
                                    .build()));
                }

                poll.setImageAnswers(imageAnswers);
            }
            case "meeting" -> {
                if (!pollRequest.getMeetingAnswers().isEmpty()) {
                    List<Long> meetingChoiceIds = poll.getMeetingAnswers().stream().map(MeetingAnswer::getId).toList();
                    List<Long> meetingChoiceIdsRequest = pollRequest.getMeetingAnswers().stream().map(MeetingAnswer::getId).filter(Objects::nonNull).toList();

                    List<Long> meetingChoiceIdsDeleted = meetingChoiceIds.stream().filter(id -> !meetingChoiceIdsRequest.contains(id)).toList();
                    List<MeetingAnswer> meetingChoiceAnswers = poll.getMeetingAnswers();
                    meetingChoiceIdsDeleted.forEach(id -> meetingChoiceAnswers.removeIf(choice -> Objects.equals(choice.getId(), id)));

                    pollRequest.getMeetingAnswers().forEach(choice -> {
                        if (choice.getId() == null) {
                            MeetingAnswer choiceAnswer = MeetingAnswer.builder()
                                    .timeFrom(choice.getTimeFrom())
                                    .timeTo(choice.getTimeTo())
                                    .poll(poll)
                                    .build();

                            meetingChoiceAnswers.add(choiceAnswer);
                        }
                    });

                    poll.setMeetingAnswers(meetingChoiceAnswers);
                }
            }
        }

        Setting setting = poll.getSetting();
        setting.setAllowMultipleOptions(pollRequest.getSetting().isAllowMultipleOptions());
        setting.setAllowComment(pollRequest.getSetting().isAllowMultipleOptions());
        setting.setRequireParticipantName(pollRequest.getSetting().isRequireParticipantName());
        setting.setVotingRestrictions(settingSelectOptionService.getByValue(pollRequest.getSetting().getVotingRestrictions().getValue()));
        setting.setDeadline(pollRequest.getSetting().getDeadline());
        setting.setAllowComment(pollRequest.getSetting().isAllowComment());
        setting.setResultsVisibility(settingSelectOptionService.getByValue(pollRequest.getSetting().getResultsVisibility().getValue()));
        setting.setAllowEditVote(pollRequest.getSetting().isAllowEditVote());

        poll.setSetting(setting);
        poll.setStatus(pollRequest.getStatus());
        poll.setComments(pollRequest.getComments());

        Poll pollResponse = pollRepository.save(poll);

        if (!filenameThumbnailOld.equals("")) {
            fileDataService.delete(filenameThumbnailOld);
        }

        if (filenameImageDeleted.size() > 0) {
            filenameImageDeleted.forEach(fileDataService::delete);
        }
        return pollResponse;
    }

    @Override
    public Poll duplicatePoll(Poll poll, String thumbnailFilename, List<String> imageAnswersFilename, List<String> labels) {
        if (thumbnailFilename != null) {
            FileData thumbnailFile = fileDataService.copy(thumbnailFilename);
            poll.setThumbnail(thumbnailFile);
        }
        if (imageAnswersFilename != null) {
            for (int i = 0; i < imageAnswersFilename.size(); i++) {
                String filename = imageAnswersFilename.get(i).replaceAll("\"", "");
                FileData imageAnswerFile = fileDataService.copy(filename);

                ImageAnswer imageAnswer = ImageAnswer.builder()
                        .image(imageAnswerFile)
                        .label(labels.get(i))
                        .poll(poll)
                        .build();
                poll.getImageAnswers().add(i, imageAnswer);
            }
        }

        return pollRepository.save(poll);
    }

    @Override
    public Poll publicPoll(String pollUuid, String userUuid) {
        Poll poll = getByUuid(pollUuid);

        if (!poll.getCreatedBy().equals(userUuid)) {
            throw new PermissionException("Sorry! You don't have permission to public this poll!");
        }
        if (poll.getStatus().equals(EPollStatus.DRAFT)) {
            if (poll.getSetting().getDeadline() != null && poll.getSetting().getDeadline().isBefore(LocalDateTime.now())) {
                poll.setStatus(EPollStatus.CLOSE);
            } else {
                poll.setStatus(EPollStatus.LIVE);
            }
        }

        return pollRepository.save(poll);
    }

    @Override
    public void resetPoll(String uuid, String userUuid) {
        Poll poll = getByUuid(uuid);

        if (!poll.getCreatedBy().equals(userUuid)) {
            throw new PermissionException("Sorry! You don't have permission to reset this poll!");
        }

        List<Vote> votesRemove = new ArrayList<>();
        switch (poll.getVotingType().getValue()) {
            case "multiple_choice" -> poll.getMultipleChoiceAnswers().forEach(answer -> {
                answer.getVoteChoices().forEach(vc -> votesRemove.add(vc.getVote()));
                answer.setVoteChoices(new ArrayList<>());
            });
            case "image" -> poll.getImageAnswers().forEach(answer -> {
                answer.getVoteChoices().forEach(vc -> votesRemove.add(vc.getVote()));
                answer.setVoteChoices(new ArrayList<>());
            });
            case "meeting" -> poll.getMeetingAnswers().forEach(answer -> {
                answer.getVoteChoices().forEach(vc -> votesRemove.add(vc.getVote()));
                answer.setVoteChoices(new ArrayList<>());
            });
        }

        poll.setVotingTokens(new ArrayList<>());
        pollRepository.save(poll);
        voteRepository.deleteAll(votesRemove);
    }

    @Override
    public void deleteByUuid(String pollUuid, String userUuid) {
        Poll poll = getByUuid(pollUuid);

        if (!poll.getCreatedBy().equals(userUuid)) {
            throw new PermissionException("Sorry! You don't have permission to reset this poll!");
        }
        pollRepository.deleteById(pollUuid);
        if (poll.getThumbnail() != null) {
            fileDataService.delete(poll.getThumbnail().getFilename());
        }
        if (poll.getVotingType().getValue().equals("image")) {
            poll.getImageAnswers().forEach(img -> fileDataService.delete(img.getImage().getFilename()));
        }
    }

    @Override
    public List<Object> searchPoll(String query, String userUuid) {
        query = "%" + query+ "%";
        return pollRepository.searchPoll(query, userUuid);
    }

    @Override
    public Poll convertToEntity(PollRequest pollRequest) {
        VotingType votingType = votingTypeService.getVotingTypeByValue(pollRequest.getVotingTypeValue());
        EPollStatus status;
        switch (pollRequest.getStatus()) {
            case "draft" -> status = EPollStatus.DRAFT;
            case "close" -> status = EPollStatus.CLOSE;
            default -> status = EPollStatus.LIVE;
        }

        Poll poll = Poll.builder()
                .title(pollRequest.getTitle())
                .description(pollRequest.getDescription())
                .votingType(votingType)
                .status(status)
                .build();

        if (pollRequest.getThumbnail() != null) {
            FileData thumbnail = fileDataService.save(pollRequest.getThumbnail());
            poll.setThumbnail(thumbnail);
        }

//        set answer choice for poll
        switch (votingType.getValue()) {
            case "multiple_choice" -> {
                List<MultipleChoiceAnswer> multipleChoices = new ArrayList<>();
                MultipleChoiceAnswer itemOther = null;

                for (int i = 0; i < pollRequest.getChoices().size(); i++) {
                    MultipleChoiceAnswerRequest item = (MultipleChoiceAnswerRequest) pollRequest.getChoices().get(i);
                    if (!item.isOther()) {
                        multipleChoices.add(MultipleChoiceAnswer.builder()
                                .id(item.getId())
                                .value(item.getValue())
                                .isOther(item.isOther())
                                .poll(poll)
                                .build());
                    } else {
                        itemOther = MultipleChoiceAnswer.builder()
                                .id(item.getId())
                                .value(item.getValue())
                                .isOther(item.isOther())
                                .poll(poll)
                                .build();
                    }
                }
                if (itemOther != null) {
                    multipleChoices.add(itemOther);
                }
                poll.setMultipleChoiceAnswers(multipleChoices);
            }
            case "image" -> {
                List<ImageAnswer> images = new ArrayList<>();

                for (int i = 0; i < pollRequest.getChoices().size(); i++) {
                    ImageAnswerRequest item = (ImageAnswerRequest) pollRequest.getChoices().get(i);
                    FileData image = fileDataService.save(item.getImage());
                    images.add(ImageAnswer.builder()
                            .label(item.getLabel())
                            .image(image)
                            .poll(poll)
                            .build());
                }

                poll.setImageAnswers(images);
            }
            case "meeting" -> {
                List<MeetingAnswer> meetings = new ArrayList<>();

                for (int i = 0; i < pollRequest.getChoices().size(); i++) {
                    MeetingAnswerRequest item = (MeetingAnswerRequest) pollRequest.getChoices().get(i);
                    meetings.add(MeetingAnswer.builder()
                            .id(item.getId())
                            .timeFrom(item.getTimeFrom())
                            .timeTo(item.getTimeTo())
                            .poll(poll)
                            .build());
                }

                poll.setMeetingAnswers(meetings);
            }
        }

//        Set _setting for poll
        Setting settingRequest = Setting.builder()
                .allowMultipleOptions(pollRequest.getSetting().isAllowMultipleOptions())
                .isRequireParticipantName(pollRequest.getSetting().isRequireParticipantName())
                .votingRestrictions(settingSelectOptionService.getByValue(pollRequest.getSetting().getVotingRestrictionValue()))
                .deadline(pollRequest.getSetting().getDeadlineTime())
                .allowComment(pollRequest.getSetting().isAllowComment())
                .resultsVisibility(settingSelectOptionService.getByValue(pollRequest.getSetting().getResultsVisibilityValue()))
                .allowEditVote(pollRequest.getSetting().isAllowEditVote())
                .build();

        poll.setSetting(settingRequest);

        return poll;
    }

    @Override
    public PollResponse convertToResponse(Poll poll) {
        int participantQuantity = voteRepository.getParticipantQuantityPoll(poll.getUuid());
        String votingTypeValue = poll.getVotingType().getValue();
        PollResponse pollResponse = PollResponse.builder()
                .uuid(poll.getUuid())
                .title(poll.getTitle())
                .description(poll.getDescription())
                .votingTypeValue(votingTypeValue)
                .ownerName(userService.getByUuid(poll.getCreatedBy()).getName())
                .createdBy(poll.getCreatedBy())
                .createdAt(poll.getCreatedAt().toString())
                .participants(participantQuantity)
                .status(poll.getStatus())
                .build();

        if (poll.getThumbnail() != null) {
            String thumbnail = poll.getThumbnail().getFilename();
            String url = fileDataService.getUrlFile(thumbnail);
            pollResponse.setThumbnail(url);
        } else {
            pollResponse.setThumbnail(null);
        }

        switch (votingTypeValue) {
            case "multiple_choice" -> {
                List<MultipleChoiceAnswerResponse> choices = new ArrayList<>();
                poll.getMultipleChoiceAnswers().forEach(c ->
                        choices.add(new MultipleChoiceAnswerResponse(c.getId(), c.getValue(), c.isOther()))
                );

                pollResponse.setChoices(choices);
            }
            case "image" -> {
                List<ImageAnswerResponse> choices = new ArrayList<>();
                poll.getImageAnswers().forEach(c -> {
                    String imageChoiceFilename = c.getImage().getFilename();
                    String imageUrl = fileDataService.getUrlFile(imageChoiceFilename);
                    choices.add(new ImageAnswerResponse(c.getId(), imageUrl, c.getLabel()));
                });

                pollResponse.setChoices(choices);
            }
            case "meeting" -> {
                List<MeetingAnswerResponse> choices = new ArrayList<>();
                poll.getMeetingAnswers().forEach(c ->
                        choices.add(new MeetingAnswerResponse(c.getId(), c.getTimeFrom().toString(), c.getTimeTo().toString()))
                );

                pollResponse.setChoices(choices);
            }
        }

        Setting setting = poll.getSetting();
        SettingResponse settingResponse = SettingResponse.builder()
                .allowMultipleOptions(setting.isAllowMultipleOptions())
                .requireParticipantName(setting.isRequireParticipantName())
                .votingRestrictionValue(setting.getVotingRestrictions().getValue())
                .deadlineTime(setting.getDeadline() == null ? null : setting.getDeadline().toString())
                .allowComment(setting.isAllowComment())
                .resultsVisibilityValue(setting.getResultsVisibility().getValue())
                .allowEditVote(setting.isAllowEditVote())
                .build();

        if (setting.getVotingRestrictions().getValue().equals("ip")) {
            settingResponse.setHasIpAddressVote(false);
        }

        if (setting.getDeadline() != null && LocalDateTime.now().isAfter(setting.getDeadline()) && !poll.getStatus().equals(EPollStatus.DRAFT)) {
            this.updateStatusByUuid(poll.getUuid(), EPollStatus.CLOSE);
            pollResponse.setStatus(EPollStatus.CLOSE);
        }

        pollResponse.setSetting(settingResponse);
        return pollResponse;
    }

    @Override
    @Transactional
    public Long castVote(String pollUuid, VoteRequest voteRequest) {
        Poll poll = getByUuid(pollUuid);
        Vote vote = new Vote();

        if (poll.getSetting().getDeadline() != null) {
            if (poll.getSetting().getDeadline().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Sorry! This Poll has already expired");
            }
        }

        if (poll.getSetting().getVotingRestrictions().getValue().equals("token")) {
            boolean checkTokenUsed = votingTokenRepository.checkTokenExist(voteRequest.getToken());
            if (checkTokenUsed) {
                votingTokenRepository.setUsedToken(voteRequest.getToken());
            } else {
                throw new BadRequestException("Invalid token! Please make sure to use a valid voting token!");
            }
        }

        if (poll.getSetting().getVotingRestrictions().getValue().equals("ip")) {
            if (voteRequest.getIpAddress() == null) {
                throw new BadRequestException("Something went wrong! Please try again.");
            }
            switch (poll.getVotingType().getValue()) {
                case "multiple_choice" -> {
                    if (voteRepository.checkIpAddressVoteMultipleAnswer(pollUuid, voteRequest.getIpAddress()) > 0) {
                        throw new BadRequestException("Sorry! You already voted on this poll!");
                    }
                }
                case "image" -> {
                    if (voteRepository.checkIpAddressVoteImageAnswer(pollUuid, voteRequest.getIpAddress()) > 0) {
                        throw new BadRequestException("Sorry! You already voted on this poll!");
                    }
                }
                case "meeting" -> {
                    if (voteRepository.checkIpAddressVoteMeetingAnswer(pollUuid, voteRequest.getIpAddress()) > 0) {
                        throw new BadRequestException("Sorry! You already voted on this poll!");
                    }
                }
            }
            vote.setIpAddress(voteRequest.getIpAddress());
        } else {
            vote.setIpAddress(voteRequest.getIpAddress());
        }

        if (poll.getSetting().getVotingRestrictions().getValue().equals("user")) {
            User user = !voteRequest.getUserUuid().equals("") ? userService.getByUuid(voteRequest.getUserUuid()) : null;

            if (user == null) {
                throw new BadRequestException("User account required! Please sign up or login to vote.");
            }
            switch (poll.getVotingType().getValue()) {
                case "multiple_choice" -> {
                    if (voteRepository.existUserVotedMultipleChoice(pollUuid, voteRequest.getUserUuid()) != 0) {
                        throw new BadRequestException("Sorry! You already voted on this poll!");
                    }
                }
                case "image" -> {
                    if (voteRepository.existUserVotedImageChoice(pollUuid, voteRequest.getUserUuid()) != 0) {
                        throw new BadRequestException("Sorry! You already voted on this poll!");
                    }
                }
                case "meeting" -> {
                    if (voteRepository.existUserVotedMeetingChoice(pollUuid, voteRequest.getUserUuid()) != 0) {
                        throw new BadRequestException("Sorry! You already voted on this poll!");
                    }
                }
            }
        }

        switch (poll.getVotingType().getValue()) {
            case "multiple_choice" -> {
                if (poll.getSetting().isAllowMultipleOptions()) {
                    List<MultipleChoiceAnswer> multipleChoiceSelected = poll.getMultipleChoiceAnswers().stream()
                            .filter(choice -> voteRequest.getChoiceIds().contains(choice.getId())).toList();

                    multipleChoiceSelected.forEach(vote::addVoteChoice);
                } else {
                    MultipleChoiceAnswer multipleChoiceAnswer = poll.getMultipleChoiceAnswers().stream()
                            .filter(choice -> choice.getId().equals(voteRequest.getChoiceIds().get(0)))
                            .findFirst().orElseThrow(() -> new ResourceNotFoundException("Vote", "id", voteRequest.getChoiceIds().get(0).toString()));
                    vote.addVoteChoice(multipleChoiceAnswer);
                }
            }
            case "image" -> {
                if (poll.getSetting().isAllowMultipleOptions()) {
                    List<ImageAnswer> imageSelected = poll.getImageAnswers().stream()
                            .filter(choice -> voteRequest.getChoiceIds().contains(choice.getId())).toList();

                    imageSelected.forEach(vote::addVoteChoice);
                } else {
                    ImageAnswer imageAnswer = poll.getImageAnswers().stream()
                            .filter(choice -> choice.getId().equals(voteRequest.getChoiceIds().get(0)))
                            .findFirst().orElseThrow(() -> new ResourceNotFoundException("Vote", "id", voteRequest.getChoiceIds().get(0).toString()));
                    vote.addVoteChoice(imageAnswer);
                }
            }
            case "meeting" -> {
                if (poll.getSetting().isAllowMultipleOptions()) {
                    List<MeetingAnswer> meetingSelected = poll.getMeetingAnswers().stream()
                            .filter(choice -> voteRequest.getChoiceIds().contains(choice.getId())).toList();
                    meetingSelected.forEach(vote::addVoteChoice);
                } else {
                    MeetingAnswer meetingAnswer = poll.getMeetingAnswers().stream()
                            .filter(choice -> choice.getId().equals(voteRequest.getChoiceIds().get(0)))
                            .findFirst().orElseThrow(() -> new ResourceNotFoundException("Vote", "id", voteRequest.getChoiceIds().get(0).toString()));
                    vote.addVoteChoice(meetingAnswer);
                }
            }
        }

        vote.setParticipant(voteRequest.getParticipant());

        if (voteRequest.getUserUuid() != null) {
            User user = !voteRequest.getUserUuid().equals("") ? userService.getByUuid(voteRequest.getUserUuid()) : null;
            vote.setUser(user);
        }

        Vote voteDb = voteRepository.save(vote);

        return voteDb.getId();
    }

    @Override
    public LastVoted getLastVotedByUser(String pollUuid, String userUuid) {
        Poll poll = getByUuid(pollUuid);
        List<Long> choiceIds = new ArrayList<>();
        Long voteId = null;
        String participant = null;

        switch (poll.getVotingType().getValue()) {
            case "multiple_choice" -> {
                voteId = voteRepository.getVoteIdMultipleLastVoted(pollUuid, userUuid);
                choiceIds = voteRepository.getLastChoiceIdsMultipleAnswerLastVoted(voteId);
            }
            case "image" -> {
                voteId = voteRepository.getVoteIdImageLastVoted(pollUuid, userUuid);
                choiceIds = voteRepository.getLastChoiceIdsImageAnswerLastVoted(voteId);
            }
            case "meeting" -> {
                voteId = voteRepository.getVoteIdMeetingLastVoted(pollUuid, userUuid);
                choiceIds = voteRepository.getLastChoiceIdsMeetingAnswerLastVoted(voteId);
            }
        }

        if (choiceIds.size() > 0) {
            switch (poll.getVotingType().getValue()) {
                case "multiple_choice" ->
                        participant = voteRepository.getParticipantMultipleLastVoted(choiceIds.get(0), voteId);
                case "image" -> participant = voteRepository.getParticipantImageLastVoted(choiceIds.get(0), voteId);
                case "meeting" -> participant = voteRepository.getParticipantMeetingLastVoted(choiceIds.get(0), voteId);
            }
        }

        return new LastVoted(choiceIds, voteId, participant);
    }

    @Override
    @Transactional
    public void editVote(String pollUuid, VoteRequest voteRequest) {
        Poll poll = getByUuid(pollUuid);
        if (poll.getSetting().getDeadline().isBefore(LocalDateTime.now())){
            throw new BadRequestException("Sorry! This Poll has already expired!");
        }
        if (voteRequest.getChoiceIds().size() == 0) {
            throw new BadRequestException("Please enter at least one answer option!");
        }
        Long voteId = -1L;
        boolean hasVoteId = false;

        if (voteRequest.getVoteId() != null) {
            voteId = voteRequest.getVoteId();
            hasVoteId = true;
        }
        if (!hasVoteId && voteRequest.getUserUuid() != null) {
            switch (poll.getVotingType().getValue()) {
                case "multiple_choice" ->
                        voteId = voteRepository.getVoteIdMultipleLastVoted(pollUuid, voteRequest.getUserUuid());
                case "image" -> voteId = voteRepository.getVoteIdImageLastVoted(pollUuid, voteRequest.getUserUuid());
                case "meeting" ->
                        voteId = voteRepository.getVoteIdMeetingLastVoted(pollUuid, voteRequest.getUserUuid());
            }
        }

        Vote vote = voteRepository.findById(voteId).orElseThrow(() -> new ResourceNotFoundException("Vote", "voteId", voteRequest.getVoteId().toString()));

        vote.setParticipant(voteRequest.getParticipant());

        switch (poll.getVotingType().getValue()) {
            case "multiple_choice" -> {
                List<MultipleChoiceAnswer> multipleChoiceSelected = new ArrayList<>();
                if (poll.getSetting().isAllowMultipleOptions()) {
                    multipleChoiceSelected = poll.getMultipleChoiceAnswers().stream()
                            .filter(choice -> voteRequest.getChoiceIds().contains(choice.getId())).toList();

                } else {
                    MultipleChoiceAnswer multipleChoiceAnswer = poll.getMultipleChoiceAnswers().stream()
                            .filter(choice -> choice.getId().equals(voteRequest.getChoiceIds().get(0)))
                            .findFirst().orElseThrow(() -> new ResourceNotFoundException("Vote", "id", voteRequest.getChoiceIds().get(0).toString()));
                    multipleChoiceSelected.add(multipleChoiceAnswer);
                }

                vote.updateChoiceMultiple(multipleChoiceSelected);
            }
            case "image" -> {
                List<ImageAnswer> imageSelected = new ArrayList<>();
                if (poll.getSetting().isAllowMultipleOptions()) {
                    imageSelected = poll.getImageAnswers().stream()
                            .filter(choice -> voteRequest.getChoiceIds().contains(choice.getId())).toList();
                } else {
                    ImageAnswer imageAnswer = poll.getImageAnswers().stream()
                            .filter(choice -> choice.getId().equals(voteRequest.getChoiceIds().get(0)))
                            .findFirst().orElseThrow(() -> new ResourceNotFoundException("Vote", "id", voteRequest.getChoiceIds().get(0).toString()));
                    imageSelected.add(imageAnswer);
                }
                vote.updateChoiceImage(imageSelected);
            }
            case "meeting" -> {
                List<MeetingAnswer> meetingSelected = new ArrayList<>();
                if (poll.getSetting().isAllowMultipleOptions()) {
                    meetingSelected = poll.getMeetingAnswers().stream()
                            .filter(choice -> voteRequest.getChoiceIds().contains(choice.getId())).toList();
                } else {
                    MeetingAnswer meetingAnswer = poll.getMeetingAnswers().stream()
                            .filter(choice -> choice.getId().equals(voteRequest.getChoiceIds().get(0)))
                            .findFirst().orElseThrow(() -> new ResourceNotFoundException("Vote", "id", voteRequest.getChoiceIds().get(0).toString()));
                    meetingSelected.add(meetingAnswer);
                }
                vote.updateChoiceMeeting(meetingSelected);
            }
        }

        voteRepository.save(vote);
    }

    @Override
    public VoteResultResponse getResultVotePoll(String pollUuid) {
        Poll poll = getByUuid(pollUuid);

        List<ChoiceVoteCount> votes = new ArrayList<>();
        List<IParticipantVoted> iParticipantVotes = new ArrayList<>();
        List<ParticipantVoted> participantVotes = new ArrayList<>();

        switch (poll.getVotingType().getValue()) {
            case "multiple_choice" -> {
                votes = voteRepository.countMultipleChoiceByPollId(pollUuid);
                iParticipantVotes = voteRepository.participantVotedMultipleChoiceByPollId(pollUuid);
            }
            case "image" -> {
                votes = voteRepository.countImageChoiceByPollId(pollUuid);
                iParticipantVotes = voteRepository.participantVotedImageChoiceByPollId(pollUuid);
            }
            case "meeting" -> {
                votes = voteRepository.countMeetingChoiceByPollId(pollUuid);
                iParticipantVotes = voteRepository.participantVotedMeetingChoiceByPollId(pollUuid);
            }
        }

        iParticipantVotes.forEach(voted ->
                participantVotes.add(new ParticipantVoted(voted.getVoteId(), voted.getParticipant(), voted.getUserUuid(),
                        Arrays.stream(voted.getChoiceIds().split(",")).map(Long::parseLong).toList()))
        );

        Long totalVote = votes.stream().mapToLong(ChoiceVoteCount::getVoteCount).sum();

        String createdBy = poll.getCreatedBy() == null ? null : userService.getByUuid(poll.getCreatedBy()).getName();
        String deadline = poll.getSetting().getDeadline() == null ? null : poll.getSetting().getDeadline().toString();

        return new VoteResultResponse(poll.getCreatedBy(), poll.getTitle(), poll.getVotingType().getValue(), createdBy, poll.getCreatedAt().toString(), poll.getSetting().getResultsVisibility().getValue(), deadline, poll.getSetting().isRequireParticipantName(), votes, participantVotes, totalVote);
    }

    @Override
    public Boolean isAllowShowResult(String pollUuid, String userUuid) {
        if (userUuid == null) {
            throw new BadRequestException("You do not have permission to see the results of this poll.");
        }
        Boolean isShowResult = null;

        Poll poll = getByUuid(pollUuid);
        String resultVisibility = poll.getSetting().getResultsVisibility().getValue();

        if (resultVisibility.equals("after_vote")) {
            switch (poll.getVotingType().getValue()) {
                case "multiple_choice" ->
                        isShowResult = voteRepository.existUserVotedMultipleChoice(pollUuid, userUuid) == 1;
                case "image" -> isShowResult = voteRepository.existUserVotedImageChoice(pollUuid, userUuid) == 1;
                case "meeting" -> isShowResult = voteRepository.existUserVotedMeetingChoice(pollUuid, userUuid) == 1;
            }
        }
        return isShowResult;
    }

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }
}