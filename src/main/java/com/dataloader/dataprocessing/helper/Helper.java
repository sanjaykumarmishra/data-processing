package com.dataloader.dataprocessing.helper;

import com.dataloader.dataprocessing.entity.PatientDetails;
import com.dataloader.dataprocessing.exceptions.InvalidDateException;
import com.dataloader.dataprocessing.repo.PatientDetailsRepo;
import com.dataloader.dataprocessing.service.PatientDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class Helper {

    @Autowired
    private PatientDetailsRepo patientDetailsRepo;

    //check that file is of excel type or not
    public static boolean checkExcelFormat(MultipartFile file) {

        String contentType = file.getContentType();



        if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return true;
        } else {
            return false;
        }

    }


    //convert excel to list of PatientDetails

    public List<PatientDetails> convertExcelToListOfPatientDetails(InputStream is) {
        List<PatientDetails> list = new ArrayList<>();

        try {


            XSSFWorkbook workbook = new XSSFWorkbook(is);
            System.out.println(workbook.getAllNames());
            XSSFSheet sheet = workbook.getSheet("data");
            int rowNumber = 0;
            Iterator<Row> iterator = sheet.iterator();
            DataFormatter formatter = new DataFormatter();
            while (iterator.hasNext()) {
                Row row = iterator.next();

                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                //To check for duplicate data and skip if data already exists in database
                String patientContactNumber = formatter.formatCellValue(row.getCell(5));
//                System.out.println(patientContactNumber);
                if(patientExistsByContactNumber(patientContactNumber)) {
                    continue;
                }

                //If DOB is null continue, since duplicate null values are getting into arraylist for null rows, applied with DOB column since null date was creating problem
                if(row.getCell(3).getStringCellValue().length()==0) continue;

                Iterator<Cell> cells = row.iterator();

                int cid = 0;

                PatientDetails p = new PatientDetails();

                while (cells.hasNext()) {
                    Cell cell = cells.next();

                    switch (cid) {
//Since ID Column is configured to be autogenerated so commented out
//                        case 0:
//                            log.warn(String.valueOf(cell.getNumericCellValue()));
//                            p.setPatientId((int) cell.getNumericCellValue());
//                            break;
                        case 1:
//                            log.warn(cell.getStringCellValue());
                            p.setPatientName(cell.getStringCellValue());
                            break;
                        case 2:
//                            log.warn(cell.getStringCellValue());
                            p.setPatientAddress(cell.getStringCellValue());
                            break;
                        case 3:
//                            String dateString = cell.getStringCellValue();
//                            log.warn(String.valueOf(dateString));
//                            p.setPatientDateofBirth(new SimpleDateFormat("MM/dd/yyyy").parse(dateString));

//                            log.warn(cell.getStringCellValue());
                            p.setPatientDateofBirth(cell.getStringCellValue());
                            break;
                        case 4:
//                            log.warn(cell.getStringCellValue());
                            p.setPatientEmail(cell.getStringCellValue());
                            break;
                        case 5:
                            //Either we can use formatter to format the numeric cell value to string
                            //or we can format the column as text directly in excel, in that case
                            //no formatter as it is used here is needed, just use getStringCellValue()
                            String number = formatter.formatCellValue(cell);
//                            log.warn(number);
                            p.setPatientContactNumber(number);
                            break;
                        case 6:
//                            log.warn(cell.getStringCellValue());
                            p.setPatientDrugId(cell.getStringCellValue());
                            break;
                        case 7:
//                            log.warn(cell.getStringCellValue());
                            p.setPatientDrugName(cell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                    cid++;

                }

                list.add(p);


            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

    }

    public boolean patientExistsByContactNumber(String patientContactNumber) {
        if(patientDetailsRepo.findByPatientContactNumber(patientContactNumber)!=null) return true;
        else return false;
    }

    public void validateDate(String dateStr) throws InvalidDateException, ParseException {
        try {
            if(dateStr.length()!=10) throw new InvalidDateException("Invalid Date Pattern: Format -> MM/dd/yyyy");
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setLenient(false);
            Date date = sdf.parse(dateStr);
            Date today = sdf.parse(sdf.format(new Date()));
//            System.out.println(today.compareTo(date));
//            System.out.println(today+"-----"+date);
            if(today.compareTo(date) < 0 || today.compareTo(date) == 0) {
                throw new InvalidDateException("DOB should be less than today's date");
            }
        } catch (ParseException e) {
            throw e;
        }


    }

}
