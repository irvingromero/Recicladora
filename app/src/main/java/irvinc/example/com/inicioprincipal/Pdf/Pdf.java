package irvinc.example.com.inicioprincipal.Pdf;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Pdf {

    private Context context;
    private File pdfFile;
    private Document documento;
    private PdfWriter pdfWriter;
    private Paragraph parrafo;

    public Pdf(Context context){
        this.context = context;
    }

    public void crearArchivo(){
        File folder = new File(Environment.getExternalStorageDirectory().toString(), "PDF");

        //// PEDIR PERMISO DE ALMACENAMIENTO!!! ////////////

        if(!folder.exists())
            folder.mkdir();
        pdfFile = new File(folder, "Reporte.pdf");
    }

    public void abrirDocumento(){
        crearArchivo();

        try{
            documento = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(documento, new FileOutputStream(pdfFile));
            documento.open();
        } catch (Exception e){
            Log.e("abrirDocumento", e.toString());
        }
    }

    public void cerrarDocumento(){
        documento.close();
    }
/*
    public void agregarTitulo(String texto){
        try {
            parrafo = new Paragraph();

            parrafoHijo(new Paragraph(texto));
            parrafo.setSpacingAfter(30);
        } catch (Exception e){
            Log.e("agregarTitulo", e.toString());
        }
    }

    private void parrafoHijo(Paragraph parrafohijo){
        parrafohijo.setAlignment(Element.ALIGN_CENTER);
        parrafo.add(parrafohijo);
    }
*/
    public void agregarParrafo(String texto){
        try{
            parrafo = new Paragraph(texto);
            parrafo.setSpacingAfter(5);
            parrafo.setSpacingBefore(5);
            documento.add(parrafo);
        }catch (Exception e){
            Log.e("agregarParrafo", e.toString());
        }
    }

    public void crearTabla(String[] header, ArrayList<String[]> clientes){
        try {
            parrafo = new Paragraph();
            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);
            PdfPCell pdfPCell;
            int indexc = 0;

            while (indexc < header.length) {
                pdfPCell = new PdfPCell(new Phrase(header[indexc++]));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.GREEN);
                pdfPTable.addCell(pdfPCell);
            }

            for(int indexR = 0; indexR < clientes.size(); indexR ++){
                String [] row = clientes.get(indexR);

                for(indexc = 0; indexc < 4; indexc ++){
                    pdfPCell = new PdfPCell(new Phrase(row[indexc]));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setFixedHeight(40);
                    pdfPTable.addCell(pdfPCell);
                }
            }
            parrafo.add(pdfPTable);
            documento.add(parrafo);

        }catch (Exception e){
            Log.e("crearTabla", e.toString());
        }
    }
}