package me.dio.credit.application.system.dto.request

import me.dio.credit.application.system.entity.Customer
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest
class CreditDtoTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should convert CreditDto to Credit entity correctly`() {
        val creditDto = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now().plusMonths(2),
            numberOfInstallments = 12,
            customerId = 1L
        )

        val credit = creditDto.toEntity()

        assertThat(credit.creditValue).isEqualTo(creditDto.creditValue)
        assertThat(credit.dayFirstInstallment).isEqualTo(creditDto.dayFirstOfInstallment)
        assertThat(credit.numberOfInstallments).isEqualTo(creditDto.numberOfInstallments)
        assertThat(credit.customer).isNotNull
        assertThat(credit.customer?.id).isEqualTo(creditDto.customerId)
        assertThat(credit.customer).isInstanceOf(Customer::class.java)
    }

    @Test
    fun `should return Credit entity with null customer if customerId is not set in CreditDto`() {
        val creditDto = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now().plusMonths(1),
            numberOfInstallments = 10,
            customerId = 99L
        )

        val credit = creditDto.toEntity()

        assertThat(credit.customer).isNotNull
        assertThat(credit.customer?.id).isEqualTo(99L)
    }

    @Test
    fun `should validate CreditDto with all valid fields`() {
        val creditDto = CreditDto(
            creditValue = BigDecimal.valueOf(5000.0),
            dayFirstOfInstallment = LocalDate.now().plusDays(1),
            numberOfInstallments = 24,
            customerId = 5L
        )

        val violations = validator.validate(creditDto)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when creditValue is null`() {
        val creditDto = CreditDto(
            creditValue = null,
            dayFirstOfInstallment = LocalDate.now().plusMonths(1),
            numberOfInstallments = 12,
            customerId = 1L
        )

        val violations = validator.validate(creditDto)

        assertThat(violations).isNotEmpty
        assertThat(violations).anyMatch { it.message == "Invalid input" && it.propertyPath.toString() == "creditValue" }
    }

    @Test
    fun `should fail validation when dayFirstOfInstallment is not in the future`() {
        val creditDtoPast = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now().minusDays(1),
            numberOfInstallments = 12,
            customerId = 1L
        )
        val creditDtoToday = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now(),
            numberOfInstallments = 12,
            customerId = 1L
        )

        val violationsPast = validator.validate(creditDtoPast)
        val violationsToday = validator.validate(creditDtoToday)

        assertThat(violationsPast).isNotEmpty
        assertThat(violationsPast).anyMatch { it.propertyPath.toString() == "dayFirstOfInstallment" }

        assertThat(violationsToday).isNotEmpty
        assertThat(violationsToday).anyMatch { it.propertyPath.toString() == "dayFirstOfInstallment" }
    }

    @Test
    fun `should fail validation when numberOfInstallments is less than 1`() {
        val creditDto = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now().plusMonths(1),
            numberOfInstallments = 0,
            customerId = 1L
        )

        val violations = validator.validate(creditDto)

        assertThat(violations).isNotEmpty
        assertThat(violations).anyMatch { it.message == "deve ser maior que ou igual à 1" && it.propertyPath.toString() == "numberOfInstallments" }
    }

    @Test
    fun `should fail validation when numberOfInstallments is greater than 48`() {
        val creditDto = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now().plusMonths(1),
            numberOfInstallments = 49,
            customerId = 1L
        )

        val violations = validator.validate(creditDto)

        assertThat(violations).isNotEmpty
        assertThat(violations).anyMatch { it.message == "deve ser menor que ou igual à 48" && it.propertyPath.toString() == "numberOfInstallments" }
    }

    @Test
    fun `should fail validation when customerId is null`() {
        val creditDto = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now().plusMonths(1),
            numberOfInstallments = 12,
            customerId = null
        )

        val violations = validator.validate(creditDto)

        assertThat(violations).isNotEmpty
        assertThat(violations).anyMatch { it.message == "Invalid input" && it.propertyPath.toString() == "customerId" }
    }
}