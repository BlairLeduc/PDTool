//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-257 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.11 at 11:22:31 AM PDT 
//


package com.cisco.dvbu.ps.deploytool.modules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Default parameter values for procedures based on their SQL type
 * 			
 * 
 * <p>Java class for RegressionDefaultProcParamsValType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegressionDefaultProcParamsValType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bit" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="varchar" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="char" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="clob" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="integer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="int" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bigint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="smallint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tinyint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="decimal" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numeric" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="real" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="float" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="double" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="binary" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="varbinary" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="blob" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="xml" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegressionDefaultProcParamsValType", propOrder = {
    "bit",
    "varchar",
    "_char",
    "clob",
    "integer",
    "_int",
    "bigint",
    "smallint",
    "tinyint",
    "decimal",
    "numeric",
    "real",
    "_float",
    "_double",
    "date",
    "time",
    "timestamp",
    "binary",
    "varbinary",
    "blob",
    "xml"
})
public class RegressionDefaultProcParamsValType {

    @XmlElement(required = true)
    protected String bit;
    @XmlElement(required = true)
    protected String varchar;
    @XmlElement(name = "char", required = true)
    protected String _char;
    @XmlElement(required = true)
    protected String clob;
    @XmlElement(required = true)
    protected String integer;
    @XmlElement(name = "int", required = true)
    protected String _int;
    @XmlElement(required = true)
    protected String bigint;
    @XmlElement(required = true)
    protected String smallint;
    @XmlElement(required = true)
    protected String tinyint;
    @XmlElement(required = true)
    protected String decimal;
    @XmlElement(required = true)
    protected String numeric;
    @XmlElement(required = true)
    protected String real;
    @XmlElement(name = "float", required = true)
    protected String _float;
    @XmlElement(name = "double", required = true)
    protected String _double;
    @XmlElement(required = true)
    protected String date;
    @XmlElement(required = true)
    protected String time;
    @XmlElement(required = true)
    protected String timestamp;
    @XmlElement(required = true)
    protected String binary;
    @XmlElement(required = true)
    protected String varbinary;
    @XmlElement(required = true)
    protected String blob;
    @XmlElement(required = true)
    protected String xml;

    /**
     * Gets the value of the bit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBit() {
        return bit;
    }

    /**
     * Sets the value of the bit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBit(String value) {
        this.bit = value;
    }

    /**
     * Gets the value of the varchar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVarchar() {
        return varchar;
    }

    /**
     * Sets the value of the varchar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVarchar(String value) {
        this.varchar = value;
    }

    /**
     * Gets the value of the char property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChar() {
        return _char;
    }

    /**
     * Sets the value of the char property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChar(String value) {
        this._char = value;
    }

    /**
     * Gets the value of the clob property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClob() {
        return clob;
    }

    /**
     * Sets the value of the clob property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClob(String value) {
        this.clob = value;
    }

    /**
     * Gets the value of the integer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInteger() {
        return integer;
    }

    /**
     * Sets the value of the integer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInteger(String value) {
        this.integer = value;
    }

    /**
     * Gets the value of the int property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInt() {
        return _int;
    }

    /**
     * Sets the value of the int property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInt(String value) {
        this._int = value;
    }

    /**
     * Gets the value of the bigint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBigint() {
        return bigint;
    }

    /**
     * Sets the value of the bigint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBigint(String value) {
        this.bigint = value;
    }

    /**
     * Gets the value of the smallint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmallint() {
        return smallint;
    }

    /**
     * Sets the value of the smallint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmallint(String value) {
        this.smallint = value;
    }

    /**
     * Gets the value of the tinyint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTinyint() {
        return tinyint;
    }

    /**
     * Sets the value of the tinyint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTinyint(String value) {
        this.tinyint = value;
    }

    /**
     * Gets the value of the decimal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDecimal() {
        return decimal;
    }

    /**
     * Sets the value of the decimal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDecimal(String value) {
        this.decimal = value;
    }

    /**
     * Gets the value of the numeric property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumeric() {
        return numeric;
    }

    /**
     * Sets the value of the numeric property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumeric(String value) {
        this.numeric = value;
    }

    /**
     * Gets the value of the real property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReal() {
        return real;
    }

    /**
     * Sets the value of the real property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReal(String value) {
        this.real = value;
    }

    /**
     * Gets the value of the float property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFloat() {
        return _float;
    }

    /**
     * Sets the value of the float property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFloat(String value) {
        this._float = value;
    }

    /**
     * Gets the value of the double property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDouble() {
        return _double;
    }

    /**
     * Sets the value of the double property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDouble(String value) {
        this._double = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestamp(String value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the binary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBinary() {
        return binary;
    }

    /**
     * Sets the value of the binary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBinary(String value) {
        this.binary = value;
    }

    /**
     * Gets the value of the varbinary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVarbinary() {
        return varbinary;
    }

    /**
     * Sets the value of the varbinary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVarbinary(String value) {
        this.varbinary = value;
    }

    /**
     * Gets the value of the blob property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBlob() {
        return blob;
    }

    /**
     * Sets the value of the blob property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlob(String value) {
        this.blob = value;
    }

    /**
     * Gets the value of the xml property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXml() {
        return xml;
    }

    /**
     * Sets the value of the xml property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXml(String value) {
        this.xml = value;
    }

}
