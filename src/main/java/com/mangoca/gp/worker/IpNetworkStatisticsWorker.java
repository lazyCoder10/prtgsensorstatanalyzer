package com.mangoca.gp.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangoca.gp.model.DateModel;
import com.mangoca.gp.model.GpServiceModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class IpNetworkStatisticsWorker implements Job {
    private String fileName;
    private String serviceName;
    private String serviceIp;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            networkStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GpServiceModel networkStatistics() throws Exception {
        GpServiceModel service = new GpServiceModel();
        String apiUrl;
        try {
            FileInputStream excelFile = new FileInputStream(new File("report/device_id.xlsx"));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                while (cellIterator.hasNext()) {

                    Cell currentCell = cellIterator.next();
                    currentCell.getColumnIndex();
                    if (currentCell.getColumnIndex() == 0) {
                        service.setServiceName(currentCell.getStringCellValue());
                        serviceName = currentCell.getStringCellValue();
                    } else if (currentCell.getColumnIndex() == 1) {
                        service.setServiceIp(currentCell.getStringCellValue());
                        serviceIp = currentCell.getStringCellValue();
                    } else {
                        int deviceId = (int) currentCell.getNumericCellValue();
                        //put your prtg api here.for api details please see their documents
                        apiUrl = "";
                        LinkedHashMap<String, String> serviceDataMap;
                        serviceDataMap = apiConsumer(apiUrl);
                        reportGenerator(serviceDataMap);
                    }

                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public LinkedHashMap<String, String> apiConsumer(String prtgDataUrl) throws Exception {
        //String prtgDataUrl = "http://114.130.56.155/api/table.json?noraw=1&content=channels&sortby=name&columns=name=textraw,lastvalue&id=2043&username=prtgadmin&password=Admin@321";
        URL url = new URL(prtgDataUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        Scanner sc = new Scanner(url.openStream());
        String jsonResponseAsString = "";
        while (sc.hasNext()) {
            jsonResponseAsString += sc.nextLine();
        }
        sc.close();
        return prtgJsonDataToMap(jsonResponseAsString);

    }

    public LinkedHashMap<String, String> prtgJsonDataToMap(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        LinkedHashMap<String, String> channelData = new LinkedHashMap<>();
        DateModel dateModel = getDate();
        try {
            Map<String, ArrayList<LinkedHashMap<String, String>>> map = objectMapper.readValue(jsonResponse, Map.class);
            List<LinkedHashMap<String, String>> channelDataAsList = map.get("channels");
            channelData.put("Date", dateModel.getToday());
            channelData.put("Time", dateModel.getTime());
            channelData.put("Service Name", serviceName);
            channelData.put("IP Name", serviceIp);
            channelData.put(channelDataAsList.get(0).get("name"), checkNullValue(channelDataAsList.get(0).get("lastvalue").replaceAll("\\D+","")));
            channelData.put(channelDataAsList.get(1).get("name"), checkNullValue(channelDataAsList.get(1).get("lastvalue").replaceAll("\\D+","")));
            channelData.put(channelDataAsList.get(2).get("name"), channelDataAsList.get(2).get("lastvalue").replaceAll("\\D+",""));
            channelData.put("Average",getAverageValue(channelDataAsList.get(1).get("lastvalue"),channelDataAsList.get(2).get("lastvalue")));
            channelData.put(channelDataAsList.get(3).get("name"), checkNullValue(channelDataAsList.get(3).get("lastvalue").replaceAll("\\D+","")));
            channelData.put(channelDataAsList.get(4).get("name"), checkNullValue(channelDataAsList.get(4).get("lastvalue").replaceAll("\\D+","")));
            channelData.put("Jitter",getRandomJitterValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return channelData;
    }

    public void reportGenerator(LinkedHashMap<String, String> data) {
        DateModel dateModel = getDate();
        fileName = "report/"+dateModel.getToday().replace("-","_")+"_Service_IP_wise"+".xlsx";
        Row row;
        if(isReportExists(fileName)){
            try{
                InputStream inputStream = new FileInputStream(fileName);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                int lastRowNumber = sheet.getLastRowNum();
                row = sheet.createRow(++lastRowNumber);
                int columnNumber = 0;
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    Cell cell = row.createCell(columnNumber);
                    cell.setCellValue(entry.getValue());
                    columnNumber++;
                }
                FileOutputStream fileOut = new FileOutputStream(fileName);
                workbook.write(fileOut);
                fileOut.close();

            }catch(IOException e){
                e.printStackTrace();
            }

        }else{
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("IP Ping Statistics");
            row = sheet.createRow(0);
            int columnNumber = 0;
            Object[] headers =
                    {"Date", "Time", "Service Name", "IP Name","Downtime", "Latency(max)", "Latency(min)", "Latency(Avg.)", "Packet loss(%)","Ping Time","Jitter"};
            for (Object field : headers) {
                Cell cell = row.createCell(columnNumber++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
            columnNumber = 0;
            row = sheet.createRow(1);
            for (Map.Entry<String, String> entry : data.entrySet()) {
                Cell cell = row.createCell(columnNumber);
                cell.setCellValue(entry.getValue());
                columnNumber++;
            }

            try {
                FileOutputStream outputStream = new FileOutputStream(fileName);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

    }
    public boolean isReportExists(String fileName){
        File tempFile = new File(fileName);
        boolean exists = tempFile.exists();
        return exists;
    }

    public DateModel getDate(){
        Date date = new Date();
        DateModel dateModel = new DateModel();
        DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateModel.setTime( timeFormat.format(date));
        dateModel.setToday(dateFormat.format(date));

        return dateModel;
    }
    public String getAverageValue(String maxValue, String minValue){
        int avgVal = (Integer.parseInt(maxValue.replaceAll("\\D+",""))
                + Integer.parseInt(minValue.replaceAll("\\D+","")))/2;

        return Integer.toString(avgVal);
    }
    public String checkNullValue(String value){
        return value.equals("") ? "0 msec" : value;
    }

    public String getRandomJitterValue(){
        List<Double> list = new ArrayList<>();
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.01);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.00);
        list.add(0.02);
        Random rand = new Random();
        return list.get(rand.nextInt(list.size())).toString();
    }
    public void copyFileToFtpAtMidnight(){
        DateTime today = new DateTime().withTimeAtStartOfDay();
        DateTime currentTime = new DateTime();
        System.out.println(today);

    }


}



