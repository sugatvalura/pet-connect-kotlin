package com.petconnect.domain.assistant

import com.petconnect.domain.assistant.KnowledgeDomain.CATS
import com.petconnect.domain.assistant.KnowledgeDomain.DOGS
import com.petconnect.domain.assistant.KnowledgeDomain.GENERAL_FAQ
import com.petconnect.domain.assistant.KnowledgeDomain.GREETINGS

/** Local knowledge base — mirrors backend/ai_assistant/knowledge/content. */
object KnowledgeBase {

    const val SCOPE_REFUSAL =
        "I can only help with Pet Connect, companion pets, and dog/cat breed questions. " +
            "Try asking about matching, listings, availability, or a specific breed."

    const val FALLBACK =
        "I'm not sure about that yet. Try asking about a dog or cat breed, how matching works, " +
            "listings, or breeding availability — or open Support from the menu."

    private fun e(id: String, domain: KnowledgeDomain, triggers: String, answer: String) =
        KnowledgeEntry(id, domain, triggers.split('|').filter(String::isNotBlank), answer)

    val entries: List<KnowledgeEntry> = greetings() + appFaq() + dogs() + cats()

    private fun greetings() = listOf(
        e("g-hi", GREETINGS, "hi|hello|hey|good morning|good afternoon|good evening|namaste|yo",
            "Hi! I'm the Pet Connect assistant. I can help with dog and cat breeds, matching nearby pets, breeding availability, listings, and how the app works. What would you like to know?"),
        e("g-how", GREETINGS, "how are you|how's it going|how r u",
            "Doing great — ready to help you find the right companion for your pet. Ask me about breeds, matching, or anything in Pet Connect!"),
        e("g-thanks", GREETINGS, "thanks|thank you|thx|appreciate it",
            "You're welcome! If you have more questions about pets, breeds, or Pet Connect, just ask."),
        e("g-bye", GREETINGS, "bye|goodbye|see you|talk later",
            "Bye! Happy matching — come back anytime you need breed tips or help with the app."),
        e("g-help", GREETINGS, "what can you do|help|how can you help|what do you know",
            "I can answer questions about dog and cat breeds, how Pet Connect matching/chats/listings work, breeding availability and privacy, and general in-app FAQs. I stay on-topic for Pet Connect and companion pets."),
    )

    private fun appFaq() = listOf(
        e("a-what", GENERAL_FAQ, "what is pet connect|what does this app do|tell me about the app|pet connect",
            "Pet Connect helps pet owners find compatible companions nearby. Create a profile for your pet, browse pets around you, match on mutual interest, chat once matched, and post pets for adoption or sale."),
        e("a-match", GENERAL_FAQ, "how does matching work|how do matches work|mutual interest|swipe|discover",
            "On Match, swipe on pets you like. When both owners show interest in each other's pets, you get a match and can chat. Compatibility scores consider breed, age, gender, distance, and health signals."),
        e("a-score", GENERAL_FAQ, "compatibility score|how is score calculated|what does % mean|compatibility",
            "The compatibility score (0–100%) weighs breed similarity, age closeness, complementary gender, distance, and vaccination/health factors. Higher scores mean a stronger nearby match for your pet."),
        e("a-add", GENERAL_FAQ, "how do i add a pet|create pet profile|register my dog|register my cat|add pet",
            "Go to Profile → Add pet. Set species, breed, gender, date of birth, photos, bio, vaccination status, and whether they're open to breeding. Location comes from your device."),
        e("a-avail", GENERAL_FAQ, "breeding availability|open to breeding|availability window|heat cycle|availability",
            "Mark a pet as available for breeding and set a date window. Visibility can be private, matches only, or nearby owners — so reproductive details stay under your control."),
        e("a-privacy", GENERAL_FAQ, "is my location private|share my address|approximate location|privacy|location",
            "Exact addresses are never shown. Distances use your location, and chat location shares are blurred to about a 1 km radius."),
        e("a-listings", GENERAL_FAQ, "how do listings work|adopt a pet|sell a pet|marketplace|listing|adopt|sell",
            "Listings let owners post pets for adoption (no price) or sale (price in rupees). Browse the Adopt tab and open a listing for details."),
        e("a-chat", GENERAL_FAQ, "how does chat work|when can i message|send photos in chat|chat",
            "Chat unlocks after a mutual match. You can send text, images, and approximate location. Report or block an owner from the chat menu."),
        e("a-notif", GENERAL_FAQ, "what notifications|alerts|nearby available|notification",
            "You'll get notified about matches, messages, nearby pets that become available, availability reminders, and listing responses."),
        e("a-filters", GENERAL_FAQ, "how do filters work|filter by breed|distance filter|filter",
            "On Nearby, open Filters to limit by distance, species, breed, gender, age range, and \"available for breeding only.\""),
        e("a-vax", GENERAL_FAQ, "vaccination status|vaccinated|partial|vaccination",
            "Vaccination status (vaccinated, partial, or not vaccinated) feeds into the health part of compatibility and helps other owners decide."),
        e("a-spay", GENERAL_FAQ, "spayed|neutered|can spayed pets breed",
            "Pets marked as spayed or neutered are not suitable for breeding. Keep breeding availability off for those pets."),
        e("a-support", GENERAL_FAQ, "contact support|report a problem|help desk|support",
            "Open the menu → Support for account help or reporting misuse. For safety issues in chat, use Report & block."),
    )
}
