package com.dna.jopt.touroptimizer.java.examples.advanced.extrainfo;

public class NodeExtraInfo {

    String phone;
    String contactPerson;
    
    public NodeExtraInfo() {
	// Nothing to do
    }

    public NodeExtraInfo(String phone, String contactPerson) {
	this.phone = phone;
	this.contactPerson = contactPerson;
    }

    public String getContactPerson() {
	return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
	this.contactPerson = contactPerson;
    }

    public String getPhone() {
	return phone;
    }

    public void setPhone(String phone) {
	this.phone = phone;
    }

    @Override
    public String toString() {
	return "Phone: " + this.phone + " / Contact: " + this.contactPerson;
    }
}
