package at.fh.swenga.report.pdf.itext5;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;

import at.fh.swenga.model.Answer;
import at.fh.swenga.model.Question;
import at.fh.swenga.model.Quiz;

public class PdfQuizReportViewItext5 extends AbstractIText5PdfView {

	/**
	 * Creates the pdf document with the quiz data, questions and answers
	 * 
	 * @author CortexLab
	 * @version 1.0
	 */
	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		// change the file name
		response.setHeader("Content-Disposition", "attachment; filename=\"quiz.pdf\"");

		List<Question> questions = (List<Question>) model.get("questions");
		Quiz quiz = (Quiz) model.get("quiz");

		Font fontHeading = FontFactory.getFont(FontFactory.HELVETICA, 20f);
		fontHeading.setColor(BaseColor.BLACK);

		Font fontTextBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f);
		fontTextBold.setColor(BaseColor.BLACK);

		Font fontText = FontFactory.getFont(FontFactory.HELVETICA, 12f);
		fontText.setColor(BaseColor.BLACK);

		addParagraph(document, quiz.getName(), fontHeading);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
		addParagraph(document, quiz.getDescription(), fontText);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		for (Question q : questions) {
			addParagraph(document, q.getQuestionText(), fontTextBold);

			com.itextpdf.text.List list = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
			list.setListSymbol("O  ");
			for (Answer a : q.getAnswerList()) {
				list.add(a.getAnswerText());
			}
			document.add(list);
			document.add(Chunk.NEWLINE);
		}

	}

	/**
	 * Helper function which adds a new paragraph to the pdf document
	 * 
	 * @author CortexLab
	 * @version 1.0
	 */
	private void addParagraph(Document document, String text, Font font) throws DocumentException {
		Paragraph p1 = new Paragraph(new Phrase(8f, text, font));
		document.add(p1);
	}

}
