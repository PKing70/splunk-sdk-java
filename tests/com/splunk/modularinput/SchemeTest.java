package com.splunk.modularinput;

import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Tests for the com.splunk.modularinput.Scheme and com.splunk.modularinput.Argument classes that define a modular
 * input scheme to send to Splunk.
 */
public class SchemeTest extends ModularInputTestCase {
    /**
     * Checks the Scheme generated by creating a Scheme object and setting no fields on it. This test checks for
     * sane defaults in the class.
     */
    @Test
    public void testGenerateXmlFromSchemeWithDefaultValues() throws TransformerException, ParserConfigurationException {
        // Generate a scheme with as many defaults in place as possible.
        Scheme scheme = new Scheme("abcd");

        Document generatedDocument = scheme.toXml();
        Document expectedDocument = resourceToXmlDocument("modularinput/data/scheme_with_defaults.xml");

        assertXmlEqual(expectedDocument, generatedDocument);
    }

    /**
     * Checks that the XML generated by a Scheme object with all its fields set and some arguments added matches
     * what we expect.
     */
    @Test
    public void testGenerateXmlFromScheme() throws TransformerException, ParserConfigurationException {
        Scheme scheme = new Scheme("abcd");
        scheme.setDescription("\uC3BC and \uC3B6 and <&> f\u00FCr");
        scheme.setStreamingMode(Scheme.StreamingMode.SIMPLE);
        scheme.setUseExternalValidation(false);
        scheme.setUseSingleInstance(true);

        Argument arg1 = new Argument("arg1");
        scheme.addArgument(arg1);

        Argument arg2 = new Argument("arg2");
        arg2.setDescription("\uC3BC and \uC3B6 and <&> f\u00FCr");
        arg2.setDataType(Argument.DataType.NUMBER);
        arg2.setRequiredOnCreate(true);
        arg2.setRequiredOnEdit(true);
        arg2.setValidation("is_pos_int('some_name')");
        scheme.addArgument(arg2);

        Document generatedDocument = scheme.toXml();
        Document expectedDocument = resourceToXmlDocument("modularinput/data/scheme_without_defaults.xml");

        assertXmlEqual(expectedDocument, generatedDocument);
    }

    /**
     * Checks that the XML produced from an Argument class that is initialized but has no additional manipulations
     * made to it is what we expect. This is mostly a check of the default values.
     */
    @Test
    public void testGenerateXmlFromArgumentWithDefaultValues() throws ParserConfigurationException, TransformerException {
        Argument argument = new Argument("some_name");

        Document generatedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        argument.addToDocument(generatedDoc, generatedDoc);

        Document expectedDoc = resourceToXmlDocument("modularinput/data/argument_with_defaults.xml");

        assertXmlEqual(expectedDoc, generatedDoc);
    }

    /**
     * Checks that the XML generated by an Argument class with all its possible values set is what we expect.
     */
    @Test
    public void testGenerateXmlFromArgument() throws ParserConfigurationException, TransformerException {
        Argument argument = new Argument("some_name");
        argument.setDescription("\uC3BC and \uC3B6 and <&> f\u00FCr");
        argument.setDataType(Argument.DataType.BOOLEAN);
        argument.setValidation("is_pos_int('some_name')");
        argument.setRequiredOnEdit(true);
        argument.setRequiredOnCreate(true);

        Document generatedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        argument.addToDocument(generatedDoc, generatedDoc);

        Document expectedDoc = resourceToXmlDocument("modularinput/data/argument_without_defaults.xml");

        assertXmlEqual(expectedDoc, generatedDoc);
    }




}