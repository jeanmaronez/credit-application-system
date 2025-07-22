package me.dio.credit.application.system.controller


import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.response.CreditView
import me.dio.credit.application.system.dto.response.CreditViewList
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enummeration.Status
import me.dio.credit.application.system.service.impl.CreditService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
class CreditResourceTest {

    @Mock
    private lateinit var creditService: CreditService

    @InjectMocks
    private lateinit var creditResource: CreditResource

    private lateinit var mockCustomer: Customer
    private lateinit var mockCredit: Credit
    private lateinit var mockCreditDto: CreditDto

    @BeforeEach
    fun setup() {
        mockCustomer = Customer(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            cpf = "12345678900",
            email = "john.doe@example.com",
            password = "password123",
            address = Address(
            zipCode = "12345-678",
            street = "Main Street",
            )
        )

        mockCredit = Credit(
            creditCode = UUID.randomUUID(),
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstInstallment = LocalDate.now().plusMonths(1),
            numberOfInstallments = 12,
            status = Status.IN_PROGRESS,
            customer = mockCustomer
        )

        mockCreditDto = CreditDto(
            creditValue = BigDecimal.valueOf(1000.0),
            dayFirstOfInstallment = LocalDate.now().plusMonths(1),
            numberOfInstallments = 12,
            customerId = mockCustomer.id!!
        )
    }

    @Test
    fun `saveCredit should return CREATED status and success message`() {
        `when`(creditService.save(anyOrNull())).thenReturn(mockCredit)

        val response = creditResource.saveCredit(mockCreditDto)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo("Credit ${mockCredit.creditCode} - Customer ${mockCredit.customer?.email} saved!")
        verify(creditService, times(1)).save(anyOrNull())
    }

    @Test
    fun `findAllByCustomerId should return OK status and list of CreditViewList`() {
        val creditList = listOf(mockCredit, mockCredit.copy(creditCode = UUID.randomUUID(), creditValue = BigDecimal.valueOf(2000.0)))
        `when`(creditService.findAllByCustomer(mockCustomer.id!!)).thenReturn(creditList)

        val response = creditResource.findAllByCustomerId(mockCustomer.id!!)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).hasSize(2)
        assertThat(response.body?.get(0)).isInstanceOf(CreditViewList::class.java)
        verify(creditService, times(1)).findAllByCustomer(mockCustomer.id!!)
    }

    @Test
    fun `findByCreditCode should return OK status and CreditView`() {
        `when`(creditService.findByCreditCode(mockCustomer.id!!, mockCredit.creditCode)).thenReturn(mockCredit)

        val response = creditResource.findByCreditCode(mockCustomer.id!!, mockCredit.creditCode)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isInstanceOf(CreditView::class.java)
        assertThat(response.body?.creditCode).isEqualTo(mockCredit.creditCode)
        verify(creditService, times(1)).findByCreditCode(mockCustomer.id!!, mockCredit.creditCode)
    }
}
