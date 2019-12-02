package irvinc.example.com.inicioprincipal.Pdf;

import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Pdf {

    private File pdfFile;
    private Document documento;
    private PdfWriter pdfWriter;
    private Paragraph parrafo;
    private Font encabezado = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);

    public void crearArchivo(){
        File folder = new File(Environment.getExternalStorageDirectory().toString(), "PDF");

        if(!folder.exists())
            folder.mkdir();
        pdfFile = new File(folder, "Reporte.pdf");
    }

    public void abrirDocumento(){
        crearArchivo();

        try{
            documento = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(documento, new FileOutputStream(pdfFile));

            pdfWriter.setPageEvent(new HeaderFooter());

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
            parrafo.setSpacingAfter(1);
            parrafo.setSpacingBefore(1);
            documento.add(parrafo);
        }catch (Exception e){
            Log.e("agregarParrafo", e.toString());
        }
    }

    public void agregarParrafoCentrado(String texto){
        try{
            parrafo = new Paragraph(texto);
            parrafo.setSpacingAfter(2);
            parrafo.setSpacingBefore(2);
            parrafo.setAlignment(Element.ALIGN_CENTER);
            documento.add(parrafo);
        }catch (Exception e){
            Log.e("agregarParrafoCentrado", e.toString());
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

                for(indexc = 0; indexc < 6; indexc ++){
                    pdfPCell = new PdfPCell(new Phrase(row[indexc]));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setFixedHeight(18);
                    pdfPTable.addCell(pdfPCell);
                }
            }
            parrafo.add(pdfPTable);
            documento.add(parrafo);

        }catch (Exception e){
            Log.e("crearTabla", e.toString());
        }
    }

    class HeaderFooter extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable tbHeader = new PdfPTable(1);
            tbHeader.setTotalWidth(documento.getPageSize().getWidth() - documento.leftMargin() - documento.rightMargin());
            tbHeader.getDefaultCell().setBorder(0);

            PdfPCell cell = new PdfPCell(new Paragraph("INSTITUTO TECNOLOGICO DE MEXICALI", encabezado));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            tbHeader.addCell(cell);
            tbHeader.writeSelectedRows(0, -1, documento.leftMargin(), pdfWriter.getPageSize().getTop(documento.topMargin()) +30, pdfWriter.getDirectContent());


            PdfPTable tbFooter = new PdfPTable(1);
            tbFooter.setTotalWidth(documento.getPageSize().getWidth() - documento.leftMargin() - documento.rightMargin());
            tbFooter.getDefaultCell().setBorder(0);
            //// MUESTRA EL NUMERO DE LA PAGINA ////
            cell = new PdfPCell(new Paragraph("" + pdfWriter.getCurrentPageNumber()));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(0);
            tbFooter.addCell(cell);
            tbFooter.writeSelectedRows(0, -1, documento.leftMargin(), pdfWriter.getPageSize().getBottom(documento.bottomMargin()) +11, pdfWriter.getDirectContent());
        }
    }
}