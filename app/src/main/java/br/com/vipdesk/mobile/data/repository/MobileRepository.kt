package br.com.vipdesk.mobile.data.repository

import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.model.Appointment
import br.com.vipdesk.mobile.data.model.MobileDashboard
import br.com.vipdesk.mobile.data.model.MobileNotification
import br.com.vipdesk.mobile.data.model.MobileTicket
import br.com.vipdesk.mobile.data.model.StatisticsCountResponse
import br.com.vipdesk.mobile.data.model.StatisticsResponse
import br.com.vipdesk.mobile.data.model.TicketListItem
import br.com.vipdesk.mobile.data.model.User

class MobileRepository(private val apiService: ApiService) {

    suspend fun getDashboard(): Result<MobileDashboard> {
        return try {
            val response = apiService.getMobileDashboard()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Erro ao carregar o dashboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(onlyUnread: Boolean): Result<Pair<List<MobileNotification>, Int>> {
        return try {
            val response = apiService.getMobileNotifications(if (onlyUnread) "unread" else null)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(body.data to body.unreadCount)
            } else {
                Result.failure(Exception(
                    if (response.code() in 500..599) "O servidor demorou para responder (${response.code()}). Toque para tentar de novo."
                    else "Erro ao carregar notificações (${response.code()})"
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(id: Int) {
        try {
            apiService.readNotification(id)
        } catch (_: Exception) {
        }
    }

    suspend fun markAllNotificationsRead() {
        try {
            apiService.readAllNotifications()
        } catch (_: Exception) {
        }
    }

    data class TicketPage(val items: List<TicketListItem>, val total: Int?)

    suspend fun getTickets(status: String, query: String, skip: Int = 0, take: Int = 80): Result<TicketPage> {
        return try {
            val response = apiService.getMobileTickets(status, query.ifBlank { null }, skip, take)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(TicketPage(body.data, body.total))
            } else {
                Result.failure(Exception("Erro ao carregar tickets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTicket(id: Int): Result<MobileTicket> {
        return try {
            val response = apiService.getMobileTicket(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao carregar o ticket"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveTicket(id: Int): Result<Unit> {
        return try {
            val response = apiService.resolveTicket(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Não foi possível resolver o ticket"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Assume o ticket para si (toUserId nulo) ou transfere para outro. */
    suspend fun claimTicket(id: Int, toUserId: Int? = null): Result<Unit> {
        return try {
            val body = if (toUserId != null) mapOf("to_user_id" to toUserId.toString()) else emptyMap()
            val response = apiService.claimTicket(id, body)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Não foi possível concluir a operação"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Filtro idêntico ao que o dashboard web envia. TODAS as chaves precisam
     * existir (mesmo null): o backend acessa filter->department_id etc. sem
     * isset, e chave ausente vira warning → 500.
     */
    private fun statsFilter(period: String) =
        """{"created":"$period","by":"company","user_id":null,"current_status":null,""" +
            """"department_id":null,"start":null,"end":null,"start_date":null,"end_date":null}"""

    /** Contadores de tickets do período — mesmo GET /statistic da web. */
    suspend fun getStatistics(period: String): Result<StatisticsResponse> {
        return try {
            val response = apiService.getStatistics(statsFilter(period), noCache = 1)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao carregar estatísticas (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Distribuições por departamento/status/canal — GET /statistic/statisticsCount. */
    suspend fun getStatisticsCount(period: String): Result<StatisticsCountResponse> {
        return try {
            val response = apiService.getStatisticsCount(statsFilter(period), noCache = 1)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao carregar distribuições"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ————— Agenda —————

    suspend fun getAppointments(startDate: String, endDate: String): Result<List<Appointment>> {
        return try {
            val response = apiService.getAppointmentsRange(startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                val root = response.body()!!
                val arr = when {
                    root.isJsonArray -> root.asJsonArray
                    root.isJsonObject && root.asJsonObject.get("data")?.isJsonArray == true ->
                        root.asJsonObject.getAsJsonArray("data")
                    else -> com.google.gson.JsonArray()
                }
                val gson = br.com.vipdesk.mobile.di.AppContainer.gson
                Result.success(arr.mapNotNull { runCatching { gson.fromJson(it, Appointment::class.java) }.getOrNull() })
            } else {
                Result.failure(Exception("Erro ao carregar agenda (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmAppointment(id: Int): Result<Unit> {
        return try {
            val response = apiService.confirmAppointment(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Não foi possível confirmar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAgents(): Result<List<User>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao carregar atendentes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
