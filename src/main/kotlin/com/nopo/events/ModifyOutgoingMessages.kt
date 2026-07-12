package com.nopo.events

interface ModifyOutgoingMessages {
    fun onChatSent(message: String): String
}