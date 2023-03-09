package com.example.radioplayer.utils

const val TAG_BY_PERIOD = "-----BY PERIOD-----"
const val TAG_BY_GENRE = "-----GENRES & TRENDS-----"
const val TAG_BY_SPECIAL = "-----SPECIAL-----"
const val TAG_BY_ORIGIN = "-----BY ORIGIN-----"
const val TAG_BY_TALK = "-----TALK, NEWS, etc-----"
const val TAG_BY_RELIGION = "-----RELIGION-----"
const val TAG_BY_OTHER = "-----OTHER-----"


val listOfTags = listOf("charts", "hits", "top")


val byPeriodTags = setOf("30s", "40s",
"50s", "60s", "70s", "80s", "90s", "00s", "10s", "decades","golden", "goldies","medieval","nostalgia",
"oldies", "oldschool", "old time radio", "retro")

val byGenreTags = setOf(
    "acoustic","adult","alternative","ambient","anime", "avant-garde",
    "bachata", "balada", "bass", "beat", "blues", "boogie", "classic","club","conservative",
    "contemporary","country", "covers",
    "dance","disco","dj", "drama", "drum and bass","dubstep",
    "eclectic","edm","electro","experimental","flamenco",
    "folk","freeform","funk","garage", "gothic","groove","guitar","hardcore", "hardstyle",
    "heavy metal","hip-hop","hiphop","house","independent","indie","industrial",
    "instrumental","jazz","jungle", "lifestyle","lo-fi","lounge",
    "mainstream","metal", "mix",
    "new age","new wave","opera","orchestral","organ", "party","patriot", "piano","pop",
    "progressive","punk","r&b","rap","reggae","remixes","rnb","rock","romance",
    "romantic","romántica","salsa",
    "schlager", "surf music", "synth", "symphony", "swing","techno",
    "traditional","traffic","trance", "trip hop", "tropical","underground","urban","vocal"
)


val specialTags = setOf("amor","atmospheric", "calm", "chill","downtempo", "fantasy", "flac", "love", "meditation", "multicultural",
"music for study", "nature", "no ads", "paranormal", "relax",
"sleep", "slow", "smooth", "soft",  "soul", "spiritual","workout")


val byOriginTags = setOf("aboriginal", "african", "afrobeats", "americana", "arabic", "asian",  "banda","bollywood",
    "caribbean", "celtic", "ethnic","greek", "indian music",
    "latin", "méxico", "national", "spanish")

val talkNewsTags = setOf("comedy","commercial", "community",
    "conspiracy theories","culture",
    "easy listening","economics","education","entertainment","entretenimiento",
    "fashion", "film",
    "football", "government", "humor",
    "information","international", "interviews","literature","live","local","movie", "news","noticias",
    "open air", "podcast",
    "politics","regional","science", "soccer", "social", "soundtrack","speech","sport",
    "storytelling","talk","travel", "tv")

val religionTags = setOf("bible","catholic","christian","christmas","gospel","islamic", "religion","religious")


val otherTags = setOf("children", "disney", "juvenil", "kids",
    "grupera", "grupero", "moi merino",  "singer-songwriter", "student", "university")





