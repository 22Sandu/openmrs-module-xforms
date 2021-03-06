/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.formentry;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptNumeric;
import org.openmrs.Drug;
import org.openmrs.Form;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.FormUtil;

/**
 * Convenience class for generating various fragments of an XML Schema for
 * OpenMRS form definitions.
 * 
 * @author Burke Mamlin
 * @version 1.0
 * @see org.openmrs.module.formentry.FormSchemaBuilder
 */
public class FormSchemaFragment {

	/**
	 * Returns XML Schema header
	 * 
	 * @param namespace
	 *            the namespace for this form
	 * @return XML Schema header
	 */
	public static String header(Form form) {
		return "<?xml version=\"1.0\"?>\n"
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
				+ "           xmlns:openmrs=\""
				+ FormEntryWrapper.getFormSchemaNamespace(form) + "\"\n"
				+ "           elementFormDefault=\"qualified\"\n"
				+ "           attributeFormDefault=\"unqualified\">\n\n";
	}

	/**
	 * Returns XML fragment for start of an OpenMRS form
	 * 
	 * @return XML fragment for start of an OpenMRS form
	 */
	public static String startForm() {
		return "<xs:element name=\"form\">\n"
				+ "  <xs:complexType>\n"
				+ "    <xs:sequence>\n"
				+ "      <xs:element name=\"header\" type=\"_header_section\" />\n";
	}

	/**
	 * Returns XML fragment to close the declaration of an OpenMRS form
	 * 
	 * @param formId
	 *            form's internal identifier
	 * @param formName
	 *            form's name
	 * @param formVersion
	 *            version of form
	 * @return XML fragment to close the declaration of an OpenMRS form
	 */
	public static String closeForm(Form form) {
		return "    </xs:sequence>\n"
				+ "    <xs:attribute name=\"id\" type=\"xs:positiveInteger\" fixed=\""
				+ form.getFormId()
				+ "\" use=\"required\" />\n"
				+ "    <xs:attribute name=\"name\" type=\"xs:string\" fixed=\""
				+ StringEscapeUtils.escapeXml(form.getName())
				+ "\" use=\"required\" />\n"
				+ "    <xs:attribute name=\"version\" type=\"xs:string\" fixed=\""
				+ form.getVersion() + "\" use=\"required\" />\n"
				+ "  </xs:complexType>\n" + "</xs:element>\n\n";
	}

	/**
	 * @return XML Schema fragment to define pre-defined datatypes
	 */
	public static String predefinedTypes() {
		return "<xs:complexType name=\"_header_section\">\n"
				+ "  <xs:sequence>\n"
				+ "    <xs:element name=\"enterer\" type=\"xs:string\" />\n"
				+ "    <xs:element name=\"date_entered\" type=\"xs:dateTime\" />\n"
				+ "    <xs:element name=\"session\" type=\"xs:string\" />\n"
				+ "    <xs:element name=\"uid\" type=\"xs:string\" />\n"
				+ "  </xs:sequence>\n"
				+ "</xs:complexType>\n\n"
				+ "<xs:complexType name=\"_other_section\">\n"
				+ "  <xs:sequence>\n"
				+ "    <xs:any namespace=\"##any\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n"
				+ "  </xs:sequence>\n"
				+ "</xs:complexType>\n\n"
				+ "<xs:simpleType name=\"_requiredString\">\n"
				+ "  <xs:restriction base=\"xs:string\">\n"
				+ "    <xs:minLength value=\"1\" />\n"
				+ "  </xs:restriction>\n"
				+ "</xs:simpleType>\n\n"
				+ "<xs:complexType name=\"_infopath_boolean\">\n"
				+ "  <xs:simpleContent>\n"
				+ "    <xs:extension base=\"xs:boolean\">\n"
				+ "      <xs:attribute name=\"infopath_boolean_hack\" type=\"xs:positiveInteger\" use=\"required\" fixed=\"1\" />\n"
				+ "    </xs:extension>\n" + "  </xs:simpleContent>\n"
				+ "</xs:complexType>\n\n";
	}

	/**
	 * Returns XML Schema fragment for a simple concept
	 * 
	 * @param token
	 *            tag name for simple concept element
	 * @param concept
	 *            OpenMRS concept referred to by this element
	 * @param xsType
	 *            XML Schema datatype
	 * @param required
	 *            <code>true</code> if element is required
	 * @return XML Schema fragment for a simple concept
	 */
	public static String simpleConcept(String token, Concept concept,
			String xsType, boolean required, Locale locale) {
		if (required && xsType.equals("xs:string"))
			xsType = "_requiredString";
		return "<xs:complexType name=\""
				+ token
				+ "\">\n"
				+ "  <xs:sequence>\n"
				+ "    <xs:element name=\"date\" type=\"xs:date\" nillable=\"true\" minOccurs=\"0\" />\n"
				+ "    <xs:element name=\"time\" type=\"xs:time\" nillable=\"true\" minOccurs=\"0\" />\n"
				+ "    <xs:element name=\"value\" type=\""
				+ xsType
				+ "\" nillable=\""
				+ (required ? "0" : "1")
				+ "\" />\n"
				+ "  </xs:sequence>\n"
				+ "  <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
				+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(concept, locale))
				+ "\" />\n"
				+ "  <xs:attribute name=\"openmrs_datatype\" type=\"xs:string\" use=\"required\" fixed=\""
				+ concept.getDatatype().getHl7Abbreviation() + "\" />\n"
				+ "</xs:complexType>\n\n";
	}

	/**
	 * Returns XML fragment for a numeric concept element
	 * 
	 * @param token
	 *            tag name for numeric concept element
	 * @param concept
	 *            OpenMRS concept referred to by this element
	 * @param required
	 *            <code>true</code> if element is required
	 * @param minInclusive
	 *            minimum value for valid response (inclusive)
	 * @param maxInclusive
	 *            maximum value for valid response (inclusive)
	 * @param precise
	 *            <code>true</code> for real numbers, <code>false</code> for
	 *            integers
	 * @return XML fragment for a numeric concept element
	 */
	public static String numericConcept(String token, ConceptNumeric concept,
			boolean required, Locale locale) {
		Double minInclusive = concept.getLowAbsolute();
		Double maxInclusive = concept.getHiAbsolute();
		Boolean precise = XformsUtil.isAllowDecimal(concept);
		boolean skipBounds = (minInclusive == null && maxInclusive == null);
		String xml = "";
		if (!skipBounds) {
			xml += "<xs:simpleType name=\""
					+ token
					+ "_restricted_type\">\n"
					+ "  <xs:restriction base=\""
					+ (precise ? "xs:float" : "xs:int")
					+ "\">\n"
					+ (minInclusive == null ? ""
							: "    <xs:minInclusive value=\""
									+ numericToString(minInclusive, precise)
									+ "\" />\n")
					+ (maxInclusive == null ? ""
							: "    <xs:maxInclusive value=\""
									+ numericToString(maxInclusive, precise)
									+ "\" />\n") + "  </xs:restriction>\n"
					+ "</xs:simpleType>\n";
		}
		xml += "<xs:complexType name=\""
				+ token
				+ "\">\n"
				+ "  <xs:sequence>\n"
				+ "    <xs:element name=\"date\" type=\"xs:date\" nillable=\"true\" minOccurs=\"0\" />\n"
				+ "    <xs:element name=\"time\" type=\"xs:time\" nillable=\"true\" minOccurs=\"0\" />\n"
				+ (skipBounds ? "    <xs:element name=\"value\" type=\""
						+ (precise ? "xs:float" : "xs:int") + "\" nillable=\""
						+ (required ? "0" : "1") + "\" />\n"
						: "    <xs:element name=\"value\" type=\"" + token
								+ "_restricted_type\" nillable=\""
								+ (required ? "0" : "1") + "\" />\n")
				+ "  </xs:sequence>\n"
				+ "  <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
				+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(concept, locale))
				+ "\" />\n"
				+ "  <xs:attribute name=\"openmrs_datatype\" type=\"xs:string\" use=\"required\" fixed=\""
				+ concept.getDatatype().getHl7Abbreviation() + "\" />\n"
				+ "</xs:complexType>\n\n";
		return xml;
	}

	/**
	 * Returns a string representing a given number. The string is formatted
	 * based upon the necessary precision (real vs. integer).
	 * 
	 * @param value
	 *            numeric value to be rendered
	 * @param precise
	 *            <code>true</code> for real numbers, <code>false</code> for
	 *            integers
	 * @return String representation of a number with given precision
	 */
	public static String numericToString(Double value, boolean precise) {
		if (precise)
			return value.toString();
		else
			return String.valueOf(Math.round(value));
	}

	/**
	 * Returns XML fragment representing a single-selection element
	 * 
	 * @param token
	 *            tagname of element
	 * @param concept
	 *            OpenMRS concept referred to by this element
	 * @param answerList
	 *            valid answers that are assumed to be non retired
	 * @param required
	 *            <code>true</code> if element is required
	 * @param locale
	 *            current context's locale
	 * @return XML fragment representing a single-selection element
	 */
	public static String selectSingle(String token, Concept concept,
			Collection<ConceptAnswer> answerList, boolean required,
			Locale locale) {
		String xml = "<xs:complexType name=\""
				+ token
				+ "\">\n"
				+ "  <xs:sequence>\n"
				+ "    <xs:element name=\"date\" type=\"xs:date\" nillable=\"true\" minOccurs=\"0\" />\n"
				+ "    <xs:element name=\"time\" type=\"xs:time\" nillable=\"true\" minOccurs=\"0\" />\n"
				+ "    <xs:element name=\"value\" minOccurs=\"0\" maxOccurs=\"1\" nillable=\""
				+ (required ? "0" : "1") + "\">\n" + "      <xs:simpleType>\n"
				+ "        <xs:restriction base=\"xs:string\">\n";
		for (ConceptAnswer answer : answerList) {
			String answerConceptName = answer.getAnswerConcept()
					.getName(locale).getName();
			if (answer.getAnswerConcept().getConceptClass().getConceptClassId()
					.equals(HL7Constants.CLASS_DRUG)
					&& answer.getAnswerDrug() != null) {
				String answerDrugName = answer.getAnswerDrug().getName();
				xml += "          <xs:enumeration value=\""
						+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(answer.getAnswerConcept(),
								locale)) + "^"
						+ FormUtil.drugToString(answer.getAnswerDrug())
						+ "\" /> <!-- " + answerDrugName + " -->\n";
			} else {
				xml += "          <xs:enumeration value=\""
						+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(answer.getAnswerConcept(),
								locale)) + "\" /> <!-- " + answerConceptName
						+ " -->\n";
			}
		}
		xml += "        </xs:restriction>\n"
				+ "      </xs:simpleType>\n"
				+ "    </xs:element>\n"
				+ "  </xs:sequence>\n"
				+ "  <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
				+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(concept, locale))
				+ "\" />\n"
				+ "  <xs:attribute name=\"openmrs_datatype\" type=\"xs:string\" use=\"required\" fixed=\""
				+ concept.getDatatype().getHl7Abbreviation()
				+ "\" />\n"
				+ "  <xs:attribute name=\"multiple\" type=\"xs:integer\" use=\"required\" fixed=\"0\" />\n"
				+ "</xs:complexType>\n\n";
		return xml;
	}

	/**
	 * Returns XML fragment representing a multiple-selection element
	 * 
	 * @param token
	 *            tagname for multiple-selection element
	 * @param concept
	 *            OpenMRS concept referred to by this element
	 * @param answerList
	 *            valid answers
	 * @param locale
	 *            context's locale
	 * @return XML fragment representing a multiple-selection element
	 */
	public static String selectMultiple(String token, Concept concept,
			Collection<ConceptAnswer> answerList, Locale locale) {
		String xml = "<xs:complexType name=\""
				+ token
				+ "\">\n"
				+ "  <xs:sequence>\n"
				+ "    <xs:element name=\"date\" type=\"xs:date\" nillable=\"true\" minOccurs=\"0\" />\n"
				+ "    <xs:element name=\"time\" type=\"xs:time\" nillable=\"true\" minOccurs=\"0\" />\n";
		
		for (ConceptAnswer answer : answerList) {
			Concept answerConcept = answer.getAnswerConcept();
			
			String answerConceptName = answerConcept.getName().getName();
			Drug answerDrug = answer.getAnswerDrug();
			if (answerDrug != null) {
				String answerDrugName = answerDrug.getName();
				xml += "    <xs:element name=\""
						+ FormUtil.getXmlToken(answerDrugName)
						+ "\" default=\"false\" nillable=\"true\">\n"
						+ "      <xs:complexType>\n"
						+ "        <xs:simpleContent>\n"
						+ "          <xs:extension base=\"xs:boolean\">\n"
						+ "            <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
						+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(answerConcept,
								locale))
						+ "^"
						+ FormUtil.drugToString(answerDrug)
						+ "\" />\n"
						// + " <xs:attribute name=\"openmrs_drug\"
						// type=\"xs:string\" use=\"required\" fixed=\""
						// + FormUtil.drugToString(answer.getAnswerDrug())
						// + "\" />\n"
						+ "          </xs:extension>\n"
						+ "        </xs:simpleContent>\n"
						+ "      </xs:complexType>\n" + "    </xs:element>\n";
			} else {
				xml += "    <xs:element name=\""
						+ FormUtil.getXmlToken(answerConceptName)
						+ "\" default=\"false\" nillable=\"true\">\n"
						+ "      <xs:complexType>\n"
						+ "        <xs:simpleContent>\n"
						+ "          <xs:extension base=\"xs:boolean\">\n"
						+ "            <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
						+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(answerConcept,
								locale)) + "\" />\n"
						+ "          </xs:extension>\n"
						+ "        </xs:simpleContent>\n"
						+ "      </xs:complexType>\n" + "    </xs:element>\n";
			}
		}
		xml += "  </xs:sequence>\n"
				+ "  <xs:attribute name=\"openmrs_concept\" type=\"xs:string\" use=\"required\" fixed=\""
				+ StringEscapeUtils.escapeXml(FormUtil.conceptToString(concept, locale))
				+ "\" />\n"
				+ "  <xs:attribute name=\"openmrs_datatype\" type=\"xs:string\" use=\"required\" fixed=\""
				+ concept.getDatatype().getHl7Abbreviation()
				+ "\" />\n"
				+ "  <xs:attribute name=\"multiple\" type=\"xs:integer\" use=\"required\" fixed=\"1\" />\n"
				+ "</xs:complexType>\n\n";
		return xml;
	}

	/**
	 * @return XML fragment for footer of XML Schema
	 */
	public static String footer() {
		return "</xs:schema>";
	}
}
