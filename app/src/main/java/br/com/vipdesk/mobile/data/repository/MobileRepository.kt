package br.com.vipdesk.mobile.data.repository

import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.model.MobileDashboard
import br.com.vipdesk.mobile.data.model.MobileNotification
import br.com.vipdesk.mobile.data.model.MobileTicket
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
                Result.failure(Exception("Erro ao carregar notificações"))
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

    suspend fun getTickets(status: String, query: String): Result<List<TicketListItem>> {
        return try {
            val response = apiService.getMobileTickets(status, query.ifBlank { null })
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
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
