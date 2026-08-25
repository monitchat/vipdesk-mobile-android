package br.com.vipdesk.mobile.data.repository

import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.model.ApiContact
import br.com.vipdesk.mobile.data.model.ApiDeal
import br.com.vipdesk.mobile.data.model.CloseDealRequest
import br.com.vipdesk.mobile.data.model.CreateContactRequest
import br.com.vipdesk.mobile.data.model.CreateDealRequest
import br.com.vipdesk.mobile.data.model.CreateTicketRequest
import br.com.vipdesk.mobile.data.model.DealPipeline
import br.com.vipdesk.mobile.data.model.Department
import br.com.vipdesk.mobile.data.model.LossReason
import br.com.vipdesk.mobile.data.model.MoveDealStageRequest
import br.com.vipdesk.mobile.data.model.Ticket
import com.google.gson.JsonParser
import retrofit2.Response

/** Contatos (CRM) e criação de tickets — endpoints reais da API v1. */
class CrmRepository(private val apiService: ApiService) {

    suspend fun searchContacts(search: String?, take: Int = 25): Result<List<ApiContact>> {
        return try {
            val response = apiService.getContacts(search?.ifBlank { null }, take)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao carregar contatos")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createContact(name: String, phone: String?, email: String?): Result<ApiContact?> {
        return try {
            val response = apiService.createContact(
                CreateContactRequest(
                    name = name,
                    phoneNumber = phone?.ifBlank { null },
                    email = email?.ifBlank { null }
                )
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao criar contato")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTicket(request: CreateTicketRequest): Result<Ticket?> {
        return try {
            val response = apiService.createTicket(request)
            if (response.isSuccessful) {
                Result.success(response.body()?.ticket)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao criar ticket")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Id da conversa existente do contato, ou null se ainda não conversou. */
    suspend fun getContactConversationId(contactId: Int): Result<Int?> {
        return try {
            val response = apiService.getContactConversation(contactId)
            if (response.isSuccessful && response.body() != null) {
                val json = response.body()!!
                val obj = if (json.has("data") && json.get("data").isJsonObject) {
                    json.getAsJsonObject("data")
                } else json
                val id = obj.get("id")?.takeIf { it.isJsonPrimitive }?.asInt
                Result.success(id)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDepartments(): Result<List<Department>> {
        return try {
            val response = apiService.getDepartments()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    // ————— Negócios (deals) —————

    suspend fun listDeals(search: String?, status: String?): Result<List<ApiDeal>> {
        return try {
            val response = apiService.getDeals(
                search = search?.ifBlank { null },
                status = status?.ifBlank { null }
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao carregar negócios")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDeal(request: CreateDealRequest): Result<ApiDeal?> {
        return try {
            val response = apiService.createDeal(request)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao criar negócio")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun moveDealStage(dealId: Int, stageId: Int): Result<ApiDeal?> {
        return try {
            val response = apiService.moveDealStage(dealId, MoveDealStageRequest(stageId))
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao mover etapa")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeDeal(dealId: Int, result: String, lostReasonId: Int? = null): Result<ApiDeal?> {
        return try {
            val response = apiService.closeDeal(dealId, CloseDealRequest(result, lostReasonId))
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao fechar negócio")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPipelines(): Result<List<DealPipeline>> {
        return try {
            val response = apiService.getPipelines()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(errorMessage(response, "Erro ao carregar pipelines")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLossReasons(): Result<List<LossReason>> {
        return try {
            val response = apiService.getLossReasons()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    /** Extrai a "message" do corpo de erro da API (ex.: contato duplicado). */
    private fun errorMessage(response: Response<*>, fallback: String): String {
        return try {
            val body = response.errorBody()?.string() ?: return fallback
            val json = JsonParser.parseString(body).asJsonObject
            json.get("message")?.takeIf { it.isJsonPrimitive }?.asString ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
