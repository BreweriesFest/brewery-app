package com.brewery.inventory.util;

import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class HelperClass {

	public static final Predicate<String> isBlankString = String::isBlank;

}
