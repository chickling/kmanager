package com.chickling.kmanager.email;

/**
 * @author Hulva Luva.H
 *
 */
public class Template {

	private static String[] thead = { "Group", "Topic", "Partition", "Lag" };

	private String partStart = "<html><body><h2 style=\"width: 100%; text-align: center;\">Kafka topic consume lag alerting</h2>";
	private StringBuilder content = new StringBuilder();

	private StringBuilder common = new StringBuilder(
			"<table style=\"border: 1px solid #ddd;padding: 8px;line-height: 1.42857143;vertical-align: top;border-top: 1px solid #ddd;border-spacing: 0;border-collapse: collapse;background-color: transparent;\">"
					+ "<thead style=\"padding: 0;background-color: #fff;\"><tr>");

	public Template() {
		appendCommon();
	}

	public void appendCommon() {

		for (String head : thead) {
			common.append("<td style=\"border: 1px solid #ddd;\">" + head + "</td>");
		}
		common.append("</tr></thead><tbody>");
	}

	public void insertTr(String trContent) {
		content.append(trContent);
	}

	public String getContent() {
		StringBuilder result = new StringBuilder();
		return result.append(partStart).append("<hr/>").append(common).append(content).append("</tbody></table>")
				.append("</body></html>").toString();
	}
}
