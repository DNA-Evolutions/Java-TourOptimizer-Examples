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
import java.time.Instant;

/**
 * The extra info of a resource in ExtraInfoExample: a phone number and a birthday.
 *
 * <p>ExtraInfoExample writes it as a JSON string into the extra info of a resource and reads it
 * back from the result. The birthday is an Instant, to show a field type beyond strings. Jackson
 * uses the constructor without arguments and the getters and setters.
 *
 * @see ExtraInfoExample
 */
public class ResourceExtraInfo {

  private String phone;
  private Instant birthday;

  /** Creates an empty object; Jackson uses it to read the JSON back. */
  public ResourceExtraInfo() {
    // Jackson fills the fields through the setters.
  }

  /**
   * Creates the extra info of a resource.
   *
   * @param phone the phone number of the resource
   * @param birthday the birthday of the resource
   */
  public ResourceExtraInfo(String phone, Instant birthday) {
    this.phone = phone;
    this.birthday = birthday;
  }

  /**
   * Returns the birthday.
   *
   * @return the birthday
   */
  public Instant getBirthday() {
    return birthday;
  }

  /**
   * Sets the birthday.
   *
   * @param birthday the birthday
   */
  public void setBirthday(Instant birthday) {
    this.birthday = birthday;
  }

  /**
   * Returns the phone number.
   *
   * @return the phone number
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the phone number.
   *
   * @param phone the phone number
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public String toString() {
    return "Phone: " + this.phone + " / Birthday: " + this.birthday;
  }
}
