package com.dna.jopt.touroptimizer.java.examples.advanced.extrainfo;

import java.time.Instant;

public class ResourceExtraInfo {

    String phone;
    Instant birthday;

    public ResourceExtraInfo() {
	// Nothing to do
    }

    public ResourceExtraInfo(String phone, Instant birthday) {
	this.phone = phone;
	this.birthday = birthday;
    }

    public Instant getBirthday() {
	return birthday;
    }

    public void setBirthday(Instant birthday) {
	this.birthday = birthday;
    }

    public String getPhone() {
	return phone;
    }

    public void setPhone(String phone) {
	this.phone = phone;
    }

    @Override
    public String toString() {
	return "Phone: " + this.phone + " / Birthday: " + this.birthday;
    }
}
