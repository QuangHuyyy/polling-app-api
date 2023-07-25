package com.example.api.helper;

import com.example.api.model.ResultVoteExport;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

public class ExcelHelper {
    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private XSSFSheet sheet;

    @Value("${application.client.url}")
    private String clientUrl = "https://quanghuy-polling-app.web.app";

    static String[] HEADERS = { "Answer Options", "Votes", "Percent" };

    private void writeInfoPoll(String title, String pollUuid){
        String linkPoll = clientUrl + "/polls/" + pollUuid;
        sheet = workbook.createSheet();

        Row rowTitle = sheet.createRow(0);
        rowTitle.setHeight((short) 700);
        Cell cellTitle = rowTitle.createCell(0);
        cellTitle.setCellValue(title);
        cellTitle.setCellStyle(createStyle(true, 22));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        sheet.autoSizeColumn(1);
        Row rowLink = sheet.createRow(1);
        Cell cellLink = rowLink.createCell(0);
        cellLink.setCellValue(linkPoll);
        CellStyle linkStyle = workbook.createCellStyle();
        XSSFFont linkFont = workbook.createFont();
        linkFont.setUnderline(XSSFFont.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.index);
        linkStyle.setFont(linkFont);

        CreationHelper helper = workbook.getCreationHelper();
        XSSFHyperlink link = (XSSFHyperlink)helper.createHyperlink(HyperlinkType.URL);
        link.setAddress(linkPoll);
        cellLink.setHyperlink(link);

        cellLink.setCellStyle(linkStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
    }

    private void writeHeaderLine(){
        Row row = sheet.createRow(3);

        CellStyle style = createStyle(true, 14);
        for (int col = 0; col < HEADERS.length; col++) {
            createCell(row, col, HEADERS[col], style);
        }
    }

    private CellStyle createStyle(boolean isBold, double fontSize){
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(isBold);
        font.setFontHeight(fontSize);
        style.setFont(font);
        return style;
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle cellStyle){
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);

        if (value instanceof Long){
            cell.setCellValue((Long) value);
        } else if (value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(cellStyle);
    }

    private void writeDataLines(List<ResultVoteExport> resultVoteExportList, Long totalVote){

        CellStyle style = createStyle(false, 14);

        int rowCount = 4;
        for (ResultVoteExport result : resultVoteExportList){
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            createCell(row, columnCount++, result.getChoice(), style);
            createCell(row, columnCount++, result.getVoteCount(), style);

            float percent = (float) result.getVoteCount() / (float) totalVote;
            NumberFormat numberFormat = NumberFormat.getPercentInstance();
            numberFormat.setMinimumFractionDigits(2);
            createCell(row, columnCount++, numberFormat.format(percent), style);
        }

        Row rowFooter = sheet.createRow(rowCount);
        createCell(rowFooter, 0, "Total Votes", createStyle(true, 14));
        createCell(rowFooter, 1, totalVote, createStyle(true, 14));
        createCell(rowFooter, 2, "100%", createStyle(true, 14));
    }

    public ByteArrayInputStream exportMultipleImageAnswer(List<ResultVoteExport> resultVoteExportList, String pollTitle, String pollUuid, Long totalVote) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            writeInfoPoll(pollTitle, pollUuid);
            writeHeaderLine();
            writeDataLines(resultVoteExportList, totalVote);

            workbook.write(outputStream);
            workbook.close();

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }

    }
}
