//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-257 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.11 at 11:22:31 AM PDT 
//


package com.cisco.dvbu.ps.deploytool.modules;

import javax.xml.bind.annotation.XmlEnum;


/**
 * <p>Java class for TriggerConditionTypeValidationList.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TriggerConditionTypeValidationList">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SYSTEM_EVENT"/>
 *     &lt;enumeration value="USER_DEFINED"/>
 *     &lt;enumeration value="JMS"/>
 *     &lt;enumeration value="TIMER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum TriggerConditionTypeValidationList {

    SYSTEM_EVENT,
    USER_DEFINED,
    JMS,
    TIMER;

    public String value() {
        return name();
    }

    public static TriggerConditionTypeValidationList fromValue(String v) {
        return valueOf(v);
    }

}
