package br.com.vipdesk.mobile.data.repository

import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.model.ApiContact
import br.com.vipdesk.mobile.data.model.ApiDeal
import br.com.vipdesk.mobile.data.model.CloseDealRequest
import br.com.vipdesk.mobile.data.model.ContactConversationSummary
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

    data class ContactPage(val items: List<ApiContact>, val total: Int)

    suspend fun searchContacts(search: String?, take: Int = 25): Result<List<ApiContact>> =
        searchContactsPage(search, take).map { it.items }

    suspend fun searchContactsPage(search: String?, take: Int = 50, skip: Int = 0): Result<ContactPage> {
        return try {
            val response = apiService.getContacts(search?.ifBlank { null }, take, skip)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(ContactPage(body.data, body.totalRecords))
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

    /** Contato por id (ContactResource do backend). */
    suspend fun getContact(contactId: Int): Result<ApiContact> {
        return try {
            val response = apiService.getContact(contactId)
            if (response.isSuccessful && response.body() != null) {
                val json = response.body()!!
                val obj = if (json.has("data") && json.get("data").isJsonObject) json.getAsJsonObject("data") else json
                Result.success(br.com.vipdesk.mobile.di.AppContainer.gson.fromJson(obj, ApiContact::class.java))
            } else {
                Result.failure(Exception(errorMessage(response, "Contato não encontrado")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Conversas do contato (ids + resumo), tolerante ao formato do resource. */
    suspend fun getContactConversations(contactId: Int): Result<List<ContactConversationSummary>> {
        return try {
            val response = apiService.getContactConversations(contactId)
            if (!response.isSuccessful || response.body() == null) return Result.success(emptyList())
            val root = response.body()!!
            val arr = when {
                root.isJsonArray -> root.asJsonArray
                root.isJsonObject && root.asJsonObject.has("data") && root.asJsonObject.get("data").isJsonArray ->
                    root.asJsonObject.getAsJsonArray("data")
                else -> return Result.success(emptyList())
            }
            val list = arr.mapNotNull { el ->
                val o = el.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
                val id = o.get("id")?.takeIf { it.isJsonPrimitive }?.asInt ?: return@mapNotNull null
                val last = o.get("last_message")?.takeIf { it.isJsonObject }?.asJsonObject
                ContactConversationSummary(
                    id = id,
                    source = o.get("source")?.takeIf { it.isJsonPrimitive }?.asString,
                    preview = last?.get("message")?.takeIf { it.isJsonPrimitive }?.asString,
                    updatedAt = last?.get("created_at")?.takeIf { it.isJsonPrimitive }?.asString
                        ?: o.get("updated_at")?.takeIf { it.isJsonPrimitive }?.asString
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ————— Negócios (deals) —————

    suspend fun listDeals(
        search: String?,
        status: String?,
        pipelineId: Int? = null,
        contactId: Int? = null,
        kanban: Boolean = false
    ): Result<List<ApiDeal>> {
        return try {
            val response = apiService.getDeals(
                search = search?.ifBlank { null },
                status = status?.ifBlank { null },
                pipelineId = pipelineId,
                contactId = contactId,
                view = if (kanban) "kanban" else null,
                take = if (kanban) 200 else 30
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

    suspend fun getDeal(dealId: Int): Result<ApiDeal> {
        return try {
            val response = apiService.getDeal(dealId)
            val deal = response.body()?.data
            if (response.isSuccessful && deal != null) Result.success(deal)
            else Result.failure(Exception(errorMessage(response, "Negócio não encontrado")))
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
