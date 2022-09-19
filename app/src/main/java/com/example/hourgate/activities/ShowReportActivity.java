package com.example.hourgate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.example.hourgate.adapter.PdfDocumentAdapter;
import com.example.hourgate.adapter.ReportAdapter;
import com.example.hourgate.models.ReportModel;
import com.example.hourgate.utils.Utils;
import com.example.hourgate.R;
import com.example.hourgate.databinding.ActivityShowReportBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ShowReportActivity extends AppCompatActivity {

    ActivityShowReportBinding binding;
    ArrayList<ReportModel> reports = new ArrayList<>();
    String months;
    String employer;
    Font orderTitleFont, orderNumberFont, titleFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showReports();

        // click listener for back to reports button
        binding.BtnBackShowReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Reports.class));
                finish();
            }
        });

        // click listener for download button
        /// https://stackoverflow.com/questions/32431723/read-external-storage-permission-for-android
        //  https://developer.android.com/reference/androidx/core/app/ActivityCompat#requestPermissions(android.app.Activity,%20java.lang.String[],%20int)

        binding.downloadReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if there is a report to download:
                if (reports.size() > 0) {

                    // determine if write external storage permissions have been granted
                    int permission = ActivityCompat.checkSelfPermission(ShowReportActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    // the permissions we need
                    String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                    // if permissions have not been granted , request for the permissions
                    if (permission != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(ShowReportActivity.this,PERMISSIONS_STORAGE, 1);
                    }
                    // if permissions have already been granted invoke the createReportPdfFile
                    else {
                        createReportPdfFile(getAppPath());
                    }
                }
                // if there is not a report record to download inform the user:
                else {
                    new Utils().showShortToast(ShowReportActivity.this, "No Report Data To Download!");
                }
            }
        });
    }


    /// https://www.youtube.com/playlist?list=PLFh8wpMiEi89IAlJLfw43YjO0DRqhOpaF
    /// https://kb.itextpdf.com/home/it5kb/examples

    /// method for creating the actual reports pdf file
    private void createReportPdfFile(String path) {

        if (new File(path).exists()) {
            new File(path).delete();
        }
        try {
            Document document = new Document();
            Log.e("path",path);
            //save
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.setMargins(20, 20, 5, 0);
            //open
            document.open();
            //settings
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addCreator("Aadil Sohail"); // was the package name hint hint jxde before this
            document.addAuthor("Hour Gate");

            //font settings
            BaseColor colorAccent = new BaseColor(0, 153, 204, 255);
            float fontSize = 8.0f;
            float valueFontSize = 10.0f;

            //custom font
            BaseFont font = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);//Add logo

            try {
                float[] columnWidth = {100f, 100f, 100f};

                PdfPTable table = new PdfPTable(columnWidth);

                table.setWidthPercentage(100);

                // get input stream
                Drawable drawable = getResources().getDrawable(R.drawable.logo);
                BitmapDrawable bmp = (BitmapDrawable) drawable;
                Bitmap bitmap = bmp.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image image = Image.getInstance(stream.toByteArray());
                image.scaleToFit(60, 60);
                image.setAlignment(Element.ALIGN_LEFT);

                //create a new cell with the specified Text and Font
                PdfPCell cell = new PdfPCell();
                cell.addElement(image);

                //set the cell alignment
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_TOP);
                cell.setBorder(Rectangle.NO_BORDER);

                table.addCell(cell);

                PdfPCell cell2 = new PdfPCell();
                //Create Title of document
                Font titleFont = new Font(font, 18.0f, Font.NORMAL, BaseColor.BLACK);
                Font title = new Font(font, 08.0f, Font.NORMAL, BaseColor.BLACK);

                // report of text in the middle
                //Chunk chunk = new Chunk("Report of " + employer + " for " + months, titleFont);
                Chunk chunk = new Chunk("Report for " + months, titleFont); // pdf/print page title
                Paragraph paragraph = new Paragraph(chunk);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                cell2.addElement(paragraph);
                cell2.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell2);

                // hour gate text on the top right
                Chunk chunk1 = new Chunk("Hour Gate", titleFont);
                Paragraph paragraph1 = new Paragraph(chunk1);
                paragraph1.setAlignment(Element.ALIGN_RIGHT);

                PdfPCell cell3 = new PdfPCell();
                cell3.setBorder(Rectangle.NO_BORDER);
                cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell3.addElement(paragraph1);
                table.addCell(cell3);

                document.add(table);

                Chunk chunk2 = new Chunk("Date/Time", orderTitleFont);
                Paragraph paragraph2 = new Paragraph(chunk2);
                paragraph2.setAlignment(Element.ALIGN_LEFT);

                Chunk chunk3 = new Chunk(new Utils().getDateTimeFromMilSeconds(System.currentTimeMillis()), orderNumberFont);
                Paragraph paragraph3 = new Paragraph(chunk3);
                paragraph3.setAlignment(Element.ALIGN_LEFT);

                PdfPCell cell4 = new PdfPCell();
                cell4.setBorder(Rectangle.NO_BORDER);
                cell4.addElement(paragraph2);
                cell4.addElement(paragraph3);

                table.addCell(cell4);


            } catch (IOException ex) {
                Toast.makeText(ShowReportActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            Font fontHeading = new Font(font, 14.0f, Font.NORMAL, BaseColor.BLACK);
            Font fontDetail = new Font(font, 10.0f, Font.NORMAL, BaseColor.BLACK);

            float[] columnWidth2 = {33.3f, 33.3f,33.3f};
            PdfPTable table1 = new PdfPTable(columnWidth2);

            table1.setWidthPercentage(100);

            //insert column headings
            insertCell(table1, "Name", Element.ALIGN_CENTER, fontHeading);
            insertCell(table1, "Hours Worked", Element.ALIGN_CENTER, fontHeading);
            insertCell(table1, "Total Wages Due", Element.ALIGN_CENTER, fontHeading);

            table1.setHeaderRows(1);

            // for on reports array list to fill in the cells in the pdf table
            for (ReportModel report : reports) {

                insertCell(table1, report.getName(), Element.ALIGN_CENTER, fontDetail);
                insertCell(table1, String.valueOf(report.getHourWorked()), Element.ALIGN_CENTER, fontDetail);
                insertCell(table1, String.valueOf(report.getTotalWages()), Element.ALIGN_CENTER, fontDetail);

            }
            document.add(table1);

            document.close();

            // once pdf is successfully generated inform the user
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

            printPDF(path);

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error :" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void insertTable(PdfPTable table, String text, int align, Font font) {
        //create a new cell with the specified Text and Font
        PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
        //set the cell alignment
        cell.setHorizontalAlignment(align);
        cell.setPadding(3f);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void insertTableNoBorder(PdfPTable table, String text, int align, Font font) {
        //create a new cell with the specified Text and Font
        PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
        //set the cell alignment
        cell.setHorizontalAlignment(align);
        cell.setPadding(3f);
        table.addCell(cell);
    }

    //https://java.hotexamples.com/site/file?hash=0xe3fa5433f476311e41bb6ee9a6219028989bddcff41e4bcac2be75700bc6489a&fullName=src/main/java/com/techvisio/eserve/PdfExample.java&project=techvisio/eServe
    // method for inserting a cell
    private void insertCell(PdfPTable table, String text, int align, Font font) {
        //create a new cell with the specified Text and Font
        PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
        //set the cell alignment
        cell.setHorizontalAlignment(align);
        cell.setPaddingBottom(8f);
        cell.setPaddingLeft(8f);
        cell.setPaddingTop(8f);
        table.addCell(cell);
    }
    /// https://stackoverflow.com/questions/33089808/print-existing-pdf-file-in-android
    /// https://developer.android.com/training/printing/custom-docs offcial docs

    // method for printing the pdf document
    private void printPDF(String completePath) {

        // Get a PrintManager instance
        PrintManager printManager = null;
        printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        try {
            // Start a print job, passing in a PrintDocumentAdapter implementation
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(ShowReportActivity.this, completePath);
            printManager.print("Report Document", printDocumentAdapter, new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    /// getting the path
    private String getAppPath() {

        String fileName = String.valueOf(System.currentTimeMillis())+".pdf";

        // https://stackoverflow.com/questions/12165381/file-constructors-explanation
        File myDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),fileName);

        // https://developer.android.com/reference/java/io/File
        // https://stackoverflow.com/questions/9820088/difference-between-mkdir-and-mkdirs-in-java-for-java-io-file#:~:text=mkdirs()%20will%20create%20the,()%20is%20like%20mkdir%20%2Dp%20.&text=new%20File(%22%2Ftmp%2F,%2Ftwo%2Fthree%22).
        if (!myDir.exists()) {
            myDir.mkdir();
        }

        return myDir.getPath();
    }

    // method for showing a generated report (reportModel) on our report recyclerview
    private void showReports() {

        reports = (ArrayList<ReportModel>) getIntent().getSerializableExtra("reports");
        employer = getIntent().getStringExtra("employer");
        months = getIntent().getStringExtra("time");

        //binding.heading.setText("Report of " + employer + " for " + months);
        binding.heading.setText("Report for " + months);

        Log.e("reportsShow", reports.size() + "");

        if (reports.size() == 0) {
            Toast.makeText(ShowReportActivity.this, "No Records Found!", Toast.LENGTH_SHORT).show();
        }

        ReportAdapter reportAdapter = new ReportAdapter(reports);
        binding.recyclerView.setAdapter(reportAdapter);

    }
}