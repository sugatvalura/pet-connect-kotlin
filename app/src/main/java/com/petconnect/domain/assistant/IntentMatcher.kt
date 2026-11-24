package com.petconnect.domain.assistant

import javax.inject.Inject

enum class KnowledgeDomain { GREETINGS, GENERAL_FAQ, DOGS, CATS }

data class KnowledgeEntry(
    val id: String,
    val domain: KnowledgeDomain,
    /** Lowercased trigger phrases. */
    val triggers: List<String>,
    val answer: String,
)

enum class AssistantIntent(val domain: KnowledgeDomain?) {
    GREETING(KnowledgeDomain.GREETINGS),
    FAREWELL(KnowledgeDomain.GREETINGS),
    THANKS(KnowledgeDomain.GREETINGS),
    HELP(KnowledgeDomain.GREETINGS),
    APP_FAQ(KnowledgeDomain.GENERAL_FAQ),
    DOG_BREED(KnowledgeDomain.DOGS),
    CAT_BREED(KnowledgeDomain.CATS),
    OUT_OF_SCOPE(null),
    UNKNOWN(KnowledgeDomain.GENERAL_FAQ),
}

data class IntentMatch(val intent: AssistantIntent, val confidence: Double, val cleanedQuery: String)

data class LocalAnswer(val reply: String, val intent: AssistantIntent, val blocked: Boolean)

/**
 * Offline intent scraping + knowledge lookup, mirroring the Python Agno
 * backend so answers stay consistent when the API is unreachable.
 */
class IntentMatcher @Inject constructor() {

    fun normalize(text: String): String = text.lowercase().trim()
        .replace('’', '\'')
        .replace(Regex("""[^\w\s'#./+-]"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()
        .split(' ')
        .joinToString(" ") { TYPOS[it] ?: it }

    fun scrape(raw: String): IntentMatch {
        val q = normalize(raw)
        if (q.isEmpty()) return IntentMatch(AssistantIntent.UNKNOWN, 0.0, q)
        if (OUT_OF_SCOPE.containsMatchIn(q)) return IntentMatch(AssistantIntent.OUT_OF_SCOPE, 0.95, q)

        var best: AssistantIntent? = null
        var bestScore = 0.0
        for ((intent, phrases) in INTENT_PHRASES) {
            val hits = phrases.filter { q.contains(it) }
            if (hits.isEmpty()) continue
            val score = hits.sumOf { it.length } + 0.15 * hits.size
            if (score > bestScore) {
                bestScore = score
                best = intent
            }
        }
        return when {
            best != null -> IntentMatch(best, minOf(0.99, 0.35 + bestScore / 40), q)
            PET_WORDS.containsMatchIn(q) -> IntentMatch(AssistantIntent.APP_FAQ, 0.4, q)
            else -> IntentMatch(AssistantIntent.UNKNOWN, 0.2, q)
        }
    }

    fun answer(raw: String): LocalAnswer {
        val match = scrape(raw)
        if (match.intent == AssistantIntent.OUT_OF_SCOPE) {
            return LocalAnswer(KnowledgeBase.SCOPE_REFUSAL, match.intent, blocked = true)
        }
        val domain = match.intent.domain
        val pool = KnowledgeBase.entries.filter {
            domain == null || it.domain == domain || it.domain == KnowledgeDomain.GREETINGS
        }
        var (entry, score) = bestEntry(pool, match.cleanedQuery)
        if (entry == null || score < 3) {
            val global = bestEntry(KnowledgeBase.entries, match.cleanedQuery)
            if (global.second > score) {
                entry = global.first
                score = global.second
            }
        }
        val reply = if (entry == null || score < 2) KnowledgeBase.FALLBACK else entry.answer
        return LocalAnswer(reply, match.intent, blocked = false)
    }

    private fun bestEntry(pool: List<KnowledgeEntry>, query: String): Pair<KnowledgeEntry?, Double> =
        pool.map { it to score(it, query) }
            .maxByOrNull { it.second }
            ?.takeIf { it.second > 0 }
            ?: (null to 0.0)

    private fun score(entry: KnowledgeEntry, query: String): Double = entry.triggers.sumOf { t ->
        if (query.contains(t)) {
            t.length + 2.0
        } else {
            // Token overlap so partial typing still lands.
            t.split(' ').count { it.length > 2 && query.contains(it) } * 1.5
        }
    }

    private companion object {
        val OUT_OF_SCOPE = Regex(
            """\b(stock|crypto|bitcoin|nft|politics|election|hack|exploit|malware|phishing|bomb|weapon|prescribe|dosage|homework|exam cheat|weather in|sports score|python script|javascript)\b""",
        )
        val PET_WORDS = Regex("""\b(pet|breed|owner|match|animal)\b""")

        val TYPOS = mapOf(
            "helo" to "hello", "hallo" to "hello", "hii" to "hi", "hiii" to "hi",
            "thanx" to "thanks", "thnx" to "thanks", "doggo" to "dog", "kitty" to "cat",
            "kittie" to "cat", "labradorr" to "labrador", "retriver" to "retriever",
            "shepard" to "shepherd", "sheperd" to "shepherd",
        )

        val INTENT_PHRASES: List<Pair<AssistantIntent, List<String>>> = listOf(
            AssistantIntent.GREETING to listOf(
                "good morning", "good afternoon", "good evening", "namaste", "hello", "hey", "hi", "yo",
                "how are you", "how's it going",
            ),
            AssistantIntent.FAREWELL to listOf("goodbye", "good bye", "see you", "talk later", "bye"),
            AssistantIntent.THANKS to listOf("thank you", "thanks", "thx", "appreciate"),
            AssistantIntent.HELP to listOf("what can you do", "how can you help", "what do you know", "help me", "help"),
            AssistantIntent.APP_FAQ to listOf(
                "pet connect", "compatibility", "matching", "mutual interest", "breeding availability",
                "availability", "swipe", "discover", "nearby", "listing", "adopt", "sell", "chat",
                "notification", "filter", "vaccination", "spayed", "neutered", "location", "privacy",
                "profile", "support", "how does", "how do i", "what is",
            ),
            AssistantIntent.DOG_BREED to listOf(
                "labrador", "golden retriever", "german shepherd", "beagle", "pug", "indie dog", "pariah",
                "desi dog", "pomeranian", "husky", "bulldog", "rottweiler", "shih tzu", "dachshund", "boxer",
                "cocker spaniel", "doberman", "dog breed", "dogs", "puppy", "dog",
            ),
            AssistantIntent.CAT_BREED to listOf(
                "persian", "siamese", "maine coon", "british shorthair", "indie cat", "desi cat", "bengal",
                "ragdoll", "sphynx", "scottish fold", "american shorthair", "cat breed", "kitten", "cats", "cat",
            ),
        )
    }
}
