package com.dna.jopt.touroptimizer.java.examples.advanced.extrainfo;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2023 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */

/**
 * The extra info of a node in ExtraInfoExample: a phone number and a contact person.
 *
 * <p>ExtraInfoExample writes it as a JSON string into the extra info of a node and reads it back
 * from the result. Jackson uses the constructor without arguments and the getters and setters.
 *
 * @see ExtraInfoExample
 */
public class NodeExtraInfo {

  private String phone;
  private String contactPerson;

  /** Creates an empty object; Jackson uses it to read the JSON back. */
  public NodeExtraInfo() {
    // Jackson fills the fields through the setters.
  }

  /**
   * Creates the extra info of a node.
   *
   * @param phone the phone number of the contact person
   * @param contactPerson the name of the contact person
   */
  public NodeExtraInfo(String phone, String contactPerson) {
    this.phone = phone;
    this.contactPerson = contactPerson;
  }

  /**
   * Returns the name of the contact person.
   *
   * @return the name of the contact person
   */
  public String getContactPerson() {
    return contactPerson;
  }

  /**
   * Sets the name of the contact person.
   *
   * @param contactPerson the name of the contact person
   */
  public void setContactPerson(String contactPerson) {
    this.contactPerson = contactPerson;
  }

  /**
   * Returns the phone number of the contact person.
   *
   * @return the phone number
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the phone number of the contact person.
   *
   * @param phone the phone number
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public String toString() {
    return "Phone: " + this.phone + " / Contact: " + this.contactPerson;
  }
}
