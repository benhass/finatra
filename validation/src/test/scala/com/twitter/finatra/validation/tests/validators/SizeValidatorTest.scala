package com.twitter.finatra.validation.tests.validators

import com.twitter.finatra.validation.ValidationResult.{Invalid, Valid}
import com.twitter.finatra.validation.{ErrorCode, Size, SizeValidator, ValidationResult, ValidatorTest}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

case class SizeArrayExample(@Size(min = 10, max = 50) sizeValue: Array[Int])
case class SizeSeqExample(@Size(min = 10, max = 50) sizeValue: Array[Int])
case class SizeInvalidTypeExample(@Size(min = 10, max = 50) sizeValue: Int)
case class SizeStringExample(@Size(min = 10, max = 140) sizeValue: String)

class SizeValidatorTest extends ValidatorTest with ScalaCheckDrivenPropertyChecks {

  test("pass validation for array type") {
    val passValue = for {
      size <- Gen.choose(10, 50)
    } yield Array.fill(size) { 0 }

    forAll(passValue) { value =>
      validate[SizeArrayExample](value).isInstanceOf[Valid] shouldBe true
    }
  }

  test("fail validation for too few array type") {
    val failValue = for {
      size <- Gen.choose(0, 9)
    } yield Array.fill(size) { 0 }

    forAll(failValue) { value =>
      validate[SizeArrayExample](value) should equal(
        Invalid(errorMessage(value), ErrorCode.SizeOutOfRange(value.length, 10, 50))
      )
    }
  }

  test("fail validation for too many array type") {
    val failValue = for {
      size <- Gen.choose(51, 100)
    } yield Array.fill(size) { 0 }

    forAll(failValue) { value =>
      validate[SizeArrayExample](value) should equal(
        Invalid(errorMessage(value), ErrorCode.SizeOutOfRange(value.length, 10, 50))
      )
    }
  }

  test("pass validation for seq type") {
    val passValue = for {
      size <- Gen.choose(10, 50)
    } yield Seq.fill(size) { 0 }

    forAll(passValue) { value =>
      validate[SizeSeqExample](value).isInstanceOf[Valid] shouldBe true
    }
  }

  test("fail validation for too few seq type") {
    val failValue = for {
      size <- Gen.choose(0, 9)
    } yield Seq.fill(size) { 0 }

    forAll(failValue) { value =>
      validate[SizeSeqExample](value) should equal(
        Invalid(errorMessage(value), ErrorCode.SizeOutOfRange(value.size, 10, 50))
      )
    }
  }

  test("fail validation for too many seq type") {
    val failValue = for {
      size <- Gen.choose(51, 100)
    } yield Seq.fill(size) { 0 }

    forAll(failValue) { value =>
      validate[SizeSeqExample](value) should equal(
        Invalid(errorMessage(value), ErrorCode.SizeOutOfRange(value.size, 10, 50))
      )
    }
  }

  test("pass validation for string type") {
    val passValue = for {
      size <- Gen.choose(10, 140)
    } yield List.fill(size) { 'a' }.mkString

    forAll(passValue) { value =>
      validate[SizeStringExample](value).isInstanceOf[Valid] shouldBe true
    }
  }

  test("fail validation for too few string type") {
    val failValue = for {
      size <- Gen.choose(0, 9)
    } yield List.fill(size) { 'a' }.mkString

    forAll(failValue) { value =>
      validate[SizeStringExample](value) should equal(
        Invalid(
          errorMessage(value, maxValue = 140),
          ErrorCode.SizeOutOfRange(value.length, 10, 140)
        )
      )
    }
  }

  test("fail for unsupported class type") {
    intercept[IllegalArgumentException] {
      validate[SizeInvalidTypeExample](2)
    }
  }

  private def validate[C: Manifest](value: Any): ValidationResult = {
    super.validate(manifest[C].runtimeClass, "sizeValue", classOf[Size], value)
  }

  private def errorMessage(value: Any, minValue: Long = 10, maxValue: Long = 50): String = {
    SizeValidator.errorMessage(messageResolver, value, minValue, maxValue)
  }

}
