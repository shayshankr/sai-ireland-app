package org.sathyasaieire.app.domain.model

data class Poll(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val isOpen: Boolean = true,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
)

data class PollWithResults(
    val poll: Poll,
    val myVoteIndex: Int? = null,
    val voteCounts: List<Int> = emptyList(),
    val totalVotes: Int = 0,
) {
    val hasVoted: Boolean get() = myVoteIndex != null

    fun percentageFor(index: Int): Float {
        if (totalVotes == 0) return 0f
        return voteCounts.getOrElse(index) { 0 }.toFloat() / totalVotes
    }

    fun countFor(index: Int): Int = voteCounts.getOrElse(index) { 0 }
}
