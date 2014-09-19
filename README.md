# XML

This module allows XML stuffs

## Dependencies

None

## Name

The module name is `mod-xml`.

## Configuration

The xml module requires no configuration

## XML Validation

### With a JSON message

You can validate a xml flow by using the following JSON message :

	{
		"xml": <xml string> |
		"url_xml": <url to an xml stream>
	}
	
Where:

* `xml` is a string containing xml to validate
* `url_xml` is an url to a xml resource

Nota bene: it is an error to pass `xml` and `url_xml` in the same message

When the xml is validate successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok"
    }  

If an error occurs in validating the document a reply is returned:

    {
        "status": "error",
        "message": <message>
    }
	
### With a Buffer message
	
You can validate a xml flow by using a Buffer message containing an xml flow.

Return message will be same as validation with JSON message

## XSLT Transformation

You can transform a xml flow with a xslt stylesheet by using the following JSON message :

	{
		"xml": <xml string> |
		"url_xml": <url to an xml stream>,
		"xsl": <xsl string> |
		"url_xsl": <url to an xsl stream>,
		"param": <list of parameters>
	}
	
Where:

* `xml` is a string containing xml to validate
* `url_xml` is an url to a xml resource
* `xsl` is a string containing xsl stylesheet
* `url_xsl` is an url to a xsl resource
* `params` contains list of parameters to pass to the xsl stylesheet

Nota bene: it is an error to pass `xml` and `url_xml` in the same message, it is an error
too to pass `xsl` and `url_xsl` in the same message.

When the xml is transform successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
		"output": <result string of transformation>
    }  

If an error occurs in transforming the xml document, a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

## XPath Execution

You can execute xpath on a xml flow by using the following JSON message :

	{
		"xml": <xml string> |
		"url_xml": <url to an xml stream>,
		"xpath": <xpath string>
	}
	
Where:

* `xml` is a string containing xml to validate
* `url_xml` is an url to a xml resource
* `xpath` is a string containing xpath to execute on xml

Nota bene: it is an error to pass `xml` and `url_xml` in the same message.

When the xpath is successfully executed, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
		"output": <result string of xpath execution>
    }  

If an error occurs in executing the xpath on the xml document, a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

## XQuery Execution

You can execute xquery on a xml flow by using the following JSON message :

	{
		"xml": <xml string> |
		"url_xml": <url to an xml stream>,
		"xquery": <xquery string>
	}
	
Where:

* `xml` is a string containing xml to validate
* `url_xml` is an url to a xml resource
* `xquery` is a string containing xquery to execute on xml

Nota bene: it is an error to pass `xml` and `url_xml` in the same message.

When the xquery is successfully executed, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
		"output": <result string of xquery execution>
    }  

If an error occurs in executing the xquery on the xml document, a reply is returned:

    {
        "status": "error",
        "message": <message>
    }
