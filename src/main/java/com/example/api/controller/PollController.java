package com.example.api.controller;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Poll;
import com.example.api.payload.request.*;
import com.example.api.payload.response.*;
import com.example.api.service.IPollService;
import com.example.api.service.IVotingTokenService;
import com.example.api.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = {"https://quanghuy-polling-app.web.app", "http://localhost:8080"}, maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollController {
    private final IPollService pollService;
    private final IVotingTokenService votingTokenService;

    @GetMapping("/user/{userUuid}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<PollResponse>> getAllPollByUser(
            @PathVariable("userUuid") String userUuid,
            @RequestParam(name = "filter", defaultValue = "created") String filter,
            @RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "created_at,desc") String[] sort
            ){
        try {
            return ResponseEntity.ok().body(pollService.getAllPollByUser(userUuid, filter, page, size, sort));
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getPoll(@PathVariable("uuid") String pollUuid){
        try {
            Poll poll = pollService.getByUuid(pollUuid);
            PollResponse pollResponse = pollService.convertToResponse(poll);

            return ResponseEntity.ok(pollResponse);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> createPoll(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestPart(name = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(name = "votingTypeValue") String votingTypeValue,
            @RequestPart(name = "multipleChoiceAnswers", required = false) List<MultipleChoiceAnswerRequest> multipleChoiceAnswers,
            @RequestPart(name = "imageAnswers", required = false) List<MultipartFile> imageAnswers,
            @RequestParam(name = "labels", required = false) List<String> labels,
            @RequestPart(name = "meetingAnswers", required = false) List<MeetingAnswerRequest> meetingAnswers,
            @RequestPart(name = "settings") SettingRequest settingRequest,
            @RequestParam(name = "status") String status
    ) {
        if (multipleChoiceAnswers != null){
            multipleChoiceAnswers.forEach(choice -> {
                if (choice.getValue() == null && !choice.isOther()){
                    choice.setOther(true);
                }
            });
        }
        PollRequest pollRequest = new PollRequest(title, description, thumbnail, votingTypeValue, multipleChoiceAnswers, imageAnswers, labels, meetingAnswers, settingRequest, status);

        Poll pollEntity = pollService.convertToEntity(pollRequest);

        Poll poll = pollService.save(pollEntity);

        return ResponseEntity.ok().body(new ResponseMessage(poll.getUuid()));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<PollResponse> updatePoll(
            @PathVariable(name = "uuid") String uuid,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestPart(name = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(name = "thumbnailStatus") String thumbnailStatus,
            @RequestParam(name = "votingTypeValue") String votingTypeValue,
            @RequestPart(name = "multipleChoiceAnswers", required = false) List<MultipleChoiceAnswerRequest> multipleChoiceAnswers,
            @RequestPart(name = "imageAnswers", required = false) List<MultipartFile> imageAnswers,
            @RequestParam(name = "labels", required = false) List<String> labels,
            @RequestPart(name = "meetingAnswers", required = false) List<MeetingAnswerRequest> meetingAnswers,
            @RequestPart(name = "settings") SettingRequest settingRequest,
            @RequestParam(name = "imageAnswerIdsNoChange", required = false) List<Long> imageAnswerIdsNoChange,
            @RequestParam(name = "status") String status
    ){
        try {
            List<String> labelsImageAnswerNoChange = new ArrayList<>();
            if (imageAnswerIdsNoChange != null){
                if (labels.size() == 0){
                    labelsImageAnswerNoChange.add("");
                } else {
                    labelsImageAnswerNoChange = labels.subList(0, imageAnswerIdsNoChange.size());
                }
            }
            if(labels != null){
                labels = labels.subList((imageAnswerIdsNoChange == null ? 0 : imageAnswerIdsNoChange.size() ), labels.size());
            }
            Poll poll = pollService.convertToEntity(new PollRequest(title, description, thumbnail, votingTypeValue, multipleChoiceAnswers, imageAnswers, labels, meetingAnswers, settingRequest, status));

            Poll pollDB = pollService.updatePoll(uuid, poll, thumbnailStatus, imageAnswerIdsNoChange, labelsImageAnswerNoChange);

            PollResponse pollResponse = pollService.convertToResponse(pollDB);
            return ResponseEntity.ok().body(pollResponse);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping("/duplicate")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<PollResponse> duplicatePoll(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestPart(name = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(name = "thumbnailFilename", required = false) String thumbnailFilename,
            @RequestParam(name = "votingTypeValue") String votingTypeValue,
            @RequestPart(name = "multipleChoiceAnswers", required = false) List<MultipleChoiceAnswerRequest> multipleChoiceAnswers,
            @RequestPart(name = "imageAnswers", required = false) List<MultipartFile> imageAnswers,
            @RequestParam(name = "labels", required = false) List<String> labels,
            @RequestPart(name = "meetingAnswers", required = false) List<MeetingAnswerRequest> meetingAnswers,
            @RequestPart(name = "settings") SettingRequest settingRequest,
            @RequestParam(name = "imageAnswersFilename", required = false) List<String> imageAnswersFilename,
            @RequestParam(name = "status") String status
    ){
        try {
            List<String> labelsImageAnswerNoChange = new ArrayList<>();
            if (imageAnswersFilename != null){
                if (labels.size() == 0){
                    labelsImageAnswerNoChange.add("");
                } else {
                    labelsImageAnswerNoChange = labels.subList(0, imageAnswersFilename.size());
                }
            }
            if(labels != null){
                if (labels.size() == 0){
                    labels.add("");
                } else {
                    labels = labels.subList((imageAnswersFilename == null ? 0 : imageAnswersFilename.size() ), labels.size());
                }
            }

            Poll poll = pollService.convertToEntity(new PollRequest(title, description, thumbnail, votingTypeValue, multipleChoiceAnswers, imageAnswers, labels, meetingAnswers, settingRequest, status));

            Poll pollDB = pollService.duplicatePoll(poll, thumbnailFilename, imageAnswersFilename, labelsImageAnswerNoChange);

            PollResponse pollResponse = pollService.convertToResponse(pollDB);
            return ResponseEntity.ok().body(pollResponse);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PutMapping("/{uuid}/public")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<PollResponse> publicPoll(@PathVariable("uuid") String pollUuid, @RequestParam() String userUuid){
        try {
            Poll poll = pollService.publicPoll(pollUuid, userUuid);
            PollResponse pollResponse = pollService.convertToResponse(poll);
            return ResponseEntity.ok().body(pollResponse);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (PermissionException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/reset/{uuid}")
    public ResponseEntity<ResponseMessage> resetPoll(@PathVariable("uuid") String pollUuid, @RequestParam() String userUuid){
        try {
            pollService.resetPoll(pollUuid, userUuid);
            return ResponseEntity.ok().body(new ResponseMessage("Successfully reset"));
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (PermissionException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ResponseMessage> deleteByUuid(@PathVariable("uuid") String pollUuid, @RequestParam() String userUuid){
        try {
            pollService.deleteByUuid(pollUuid, userUuid);
            return ResponseEntity.ok().body(new ResponseMessage("Delete poll successfully!"));
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (PermissionException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<Object>> searchPollByTitleAndDescription(@PathVariable("query") String query,
                                                                        @RequestParam("userUuid") String userUuid){
        return ResponseEntity.ok().body(pollService.searchPoll(query, userUuid));
    }

    @PostMapping("/{uuid}/vote")
    public ResponseEntity<ResponseMessage> castVote(@PathVariable("uuid") String uuid, @RequestBody VoteRequest voteRequest){
        try{
            Long voteId = pollService.castVote(uuid, voteRequest);
            if (voteRequest.getUserUuid() == null || voteRequest.getUserUuid().equals("")){
                return ResponseEntity.ok().body(new ResponseMessage("voteId:" + voteId));
            }
            return ResponseEntity.ok().body(new ResponseMessage("Vote successfully."));
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }


    @PutMapping("/{uuid}/vote")
    public ResponseEntity<ResponseMessage> editVote(@PathVariable("uuid") String uuid,
                                                    @RequestBody VoteRequest voteRequest){
        try {
            pollService.editVote(uuid, voteRequest);
            return ResponseEntity.ok().body(new ResponseMessage("Update vote successfully."));
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/{uuid}/user-vote-last")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LastVoted> getLastVotedByUser(@PathVariable("uuid") String uuid,
                                                         @RequestParam(name = "userUuid")String userUuid){
        try {
            LastVoted lastVoted = pollService.getLastVotedByUser(uuid, userUuid);
            return ResponseEntity.ok().body(lastVoted);
        }  catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping("/{uuid}/send-token-to-email")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> sendEmail(@PathVariable("uuid") String uuid,
                                                     @RequestParam(name = "userUuid") String userUuid,
                                                     @RequestParam(name = "emailAddresses") String emailAddressesRequest,
                                                     @RequestParam(name = "ownerEmail") String ownerEmail,
                                                     @RequestParam(name = "ownerName") String ownerName){
        try {
            emailAddressesRequest = emailAddressesRequest.replaceAll(" ", "");
            if (emailAddressesRequest.startsWith(";")){
                emailAddressesRequest = emailAddressesRequest.substring(1);
            }

            if (emailAddressesRequest.endsWith(";")){
                emailAddressesRequest = emailAddressesRequest.substring(0, emailAddressesRequest.length() - 1);
            }

            List<String> emailAddresses = List.of((emailAddressesRequest.split(";")));

            String message = votingTokenService.generateToken(userUuid, uuid, emailAddresses, ownerEmail, ownerName);

            return ResponseEntity.ok().body(new ResponseMessage(message));
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/{uuid}/tokens-sent")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<VotingTokenResponse>> getAllTokensSent(@PathVariable("uuid") String uuid){
        List<VotingTokenResponse> votingTokenResponses = votingTokenService.getAllTokenOfPoll(uuid);
        return ResponseEntity.ok().body(votingTokenResponses);
    }

    @DeleteMapping("/token-sent/{tokenId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> deleteTokenSent(@PathVariable("tokenId") Long tokenId){
        try {
            votingTokenService.deleteTokenById(tokenId);
            return ResponseEntity.ok().body(new ResponseMessage("Delete token successfully."));
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/{uuid}/result")
    public ResponseEntity<VoteResultResponse> showResult(@PathVariable("uuid") String uuid){
        try {
            return ResponseEntity.ok().body(pollService.getResultVotePoll(uuid));
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/{uuid}/allow-show-result")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> allowShowResult(@PathVariable("uuid") String pollUuid,
                                                   @RequestParam(name = "userUuid", required = false) String userUuid){
        try {
            return ResponseEntity.ok().body(pollService.isAllowShowResult(pollUuid, userUuid));
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/{uuid}/tokens")
    public ResponseEntity<List<VotingTokenResponse>> getAllTokens(@PathVariable("uuid") String uuid){
        return ResponseEntity.ok().body(votingTokenService.getAllTokenOfPoll(uuid));
    }
}