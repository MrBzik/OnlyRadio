package com.example.radioplayer.utils

import com.example.radioplayer.adapters.models.TagWithGenre

const val TAG_BY_PERIOD = "-BY PERIOD-"
const val TAG_BY_GENRE = "-GENRES & TRENDS-"
const val TAG_BY_SPECIAL = "-SPECIAL-"
const val TAG_BY_ORIGIN = "-BY ORIGIN-"
const val TAG_BY_TALK = "-TALK, NEWS, etc-"
const val TAG_BY_RELIGION = "-RELIGION-"
const val TAG_BY_OTHER = "-OTHER-"



val listOfTagsSimple = listOf("charts", "hits", "top")

val listOfTags = setOf(TagWithGenre.Tag("charts"), TagWithGenre.Tag("hits"), TagWithGenre.Tag("top"))


val byPeriodTagsSimple = setOf("30s", "40s",
"50s", "60s", "70s", "80s", "90s", "00s", "10s", "decades","golden", "goldies","medieval","nostalgia",
"oldies", "oldschool", "old time radio", "retro")

val tagsListByPeriod = setOf(
    TagWithGenre.Tag("30s"), TagWithGenre.Tag("40s"), TagWithGenre.Tag("50s"),
    TagWithGenre.Tag("60s"), TagWithGenre.Tag("70s"), TagWithGenre.Tag("80s"),
    TagWithGenre.Tag("90s"), TagWithGenre.Tag("00s"), TagWithGenre.Tag("10s"),
    TagWithGenre.Tag("decades"), TagWithGenre.Tag("golden"),
    TagWithGenre.Tag("goldies"),TagWithGenre.Tag("medieval"),
    TagWithGenre.Tag("nostalgia"), TagWithGenre.Tag("oldies"),
    TagWithGenre.Tag("oldschool"), TagWithGenre.Tag("old time radio"), TagWithGenre.Tag("retro")
)

val byGenreTagsSimple = setOf(
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




)


val tagsListByGenre = setOf(
    TagWithGenre.Tag("acoustic"),
    TagWithGenre.Tag("adult"),
    TagWithGenre.Tag("alternative"),
    TagWithGenre.Tag("ambient"),
    TagWithGenre.Tag("anime"),
    TagWithGenre.Tag("avant-garde"),
    TagWithGenre.Tag("bachata"),
    TagWithGenre.Tag("balada"),
    TagWithGenre.Tag("bass"),
    TagWithGenre.Tag("beat"),
    TagWithGenre.Tag("blues"),
    TagWithGenre.Tag("boogie"),
    TagWithGenre.Tag("classic"),
    TagWithGenre.Tag("club"),
    TagWithGenre.Tag("conservative"),
    TagWithGenre.Tag("contemporary"),
    TagWithGenre.Tag("country"),
    TagWithGenre.Tag("covers"),
    TagWithGenre.Tag("dance"),
    TagWithGenre.Tag("disco"),
    TagWithGenre.Tag("dj"),
    TagWithGenre.Tag("drama"),
    TagWithGenre.Tag("drum and bass"),
    TagWithGenre.Tag("dubstep"),
    TagWithGenre.Tag("eclectic"),
    TagWithGenre.Tag("edm"),
    TagWithGenre.Tag("electro"),
    TagWithGenre.Tag("experimental"),
    TagWithGenre.Tag("flamenco"),
    TagWithGenre.Tag("folk"),
    TagWithGenre.Tag("freeform"),
    TagWithGenre.Tag("funk"),
    TagWithGenre.Tag("garage"),
    TagWithGenre.Tag("gothic"),
    TagWithGenre.Tag("groove"),
    TagWithGenre.Tag("guitar"),
    TagWithGenre.Tag("hardcore"),
    TagWithGenre.Tag("hardstyle"),
    TagWithGenre.Tag("heavy metal"),
    TagWithGenre.Tag("hip-hop"),
    TagWithGenre.Tag("hiphop"),
    TagWithGenre.Tag("house"),
    TagWithGenre.Tag("independent"),
    TagWithGenre.Tag("indie"),
    TagWithGenre.Tag("industrial"),
    TagWithGenre.Tag("instrumental"),
    TagWithGenre.Tag("jazz"),
    TagWithGenre.Tag("jungle"),
    TagWithGenre.Tag("lifestyle"),
    TagWithGenre.Tag("lo-fi"),
    TagWithGenre.Tag("lounge"),
    TagWithGenre.Tag("mainstream"),
    TagWithGenre.Tag("metal"),
    TagWithGenre.Tag("mix"),
    TagWithGenre.Tag("new age"),
    TagWithGenre.Tag("new wave"),
    TagWithGenre.Tag("opera"),
    TagWithGenre.Tag("orchestral"),
    TagWithGenre.Tag("organ"),
    TagWithGenre.Tag("party"),
    TagWithGenre.Tag("patriot"),
    TagWithGenre.Tag("piano"),
    TagWithGenre.Tag("pop"),
    TagWithGenre.Tag("progressive"),
    TagWithGenre.Tag("punk"),
    TagWithGenre.Tag("r&b"),
    TagWithGenre.Tag("rap"),
    TagWithGenre.Tag("reggae"),
    TagWithGenre.Tag("remixes"),
    TagWithGenre.Tag("rnb"),
    TagWithGenre.Tag("rock"),
    TagWithGenre.Tag("romance"),
    TagWithGenre.Tag("romantic"),
    TagWithGenre.Tag("romántica"),
    TagWithGenre.Tag("salsa"),
    TagWithGenre.Tag("schlager"),
    TagWithGenre.Tag("surf music"),
    TagWithGenre.Tag("synth"),
    TagWithGenre.Tag("symphony"),
    TagWithGenre.Tag("swing"),
    TagWithGenre.Tag("techno"),
    TagWithGenre.Tag("traditional"),
    TagWithGenre.Tag("traffic"),
    TagWithGenre.Tag("trance"),
    TagWithGenre.Tag("trip hop"),
    TagWithGenre.Tag("tropical"),
    TagWithGenre.Tag("underground"),
    TagWithGenre.Tag("urban"),
    TagWithGenre.Tag("vocal")
)


val specialTagsSimple = setOf("amor","atmospheric", "calm", "chill","downtempo", "fantasy", "flac", "love", "meditation", "multicultural",
"music for study", "nature", "no ads", "paranormal", "relax",
"sleep", "slow", "smooth", "soft",  "soul", "spiritual","workout")

val tagsListSpecial = setOf(
    TagWithGenre.Tag("amor"),
    TagWithGenre.Tag("atmospheric"),
    TagWithGenre.Tag("calm"),
    TagWithGenre.Tag("chill"),
    TagWithGenre.Tag("downtempo"),
    TagWithGenre.Tag("fantasy"),
    TagWithGenre.Tag("flac"),
    TagWithGenre.Tag("love"),
    TagWithGenre.Tag("meditation"),
    TagWithGenre.Tag("multicultural"),
    TagWithGenre.Tag("music for study"),
    TagWithGenre.Tag("nature"),
    TagWithGenre.Tag("no ads"),
    TagWithGenre.Tag("paranormal"),
    TagWithGenre.Tag("relax"),
    TagWithGenre.Tag("sleep"),
    TagWithGenre.Tag("slow"),
    TagWithGenre.Tag("smooth"),
    TagWithGenre.Tag("soft"),
    TagWithGenre.Tag("soul"),
    TagWithGenre.Tag("spiritual"),
    TagWithGenre.Tag("workout")
)



val byOriginTagsSimple = setOf("aboriginal", "african", "afrobeats", "americana", "arabic", "asian",  "banda","bollywood",
    "caribbean", "celtic", "ethnic","greek", "indian music",
    "latin", "méxico", "national", "spanish")


val tagsListByOrigin = setOf(
    TagWithGenre.Tag("aboriginal"),
    TagWithGenre.Tag("african"),
    TagWithGenre.Tag("afrobeats"),
    TagWithGenre.Tag("americana"),
    TagWithGenre.Tag("arabic"),
    TagWithGenre.Tag("asian"),
    TagWithGenre.Tag("banda"),
    TagWithGenre.Tag("bollywood"),
    TagWithGenre.Tag("caribbean"),
    TagWithGenre.Tag("celtic"),
    TagWithGenre.Tag("ethnic"),
    TagWithGenre.Tag("greek"),
    TagWithGenre.Tag("indian music"),
    TagWithGenre.Tag("latin"),
    TagWithGenre.Tag("méxico"),
    TagWithGenre.Tag("national"),
    TagWithGenre.Tag("spanish")
)



val talkNewsTagsSimple = setOf("comedy","commercial", "community",
    "conspiracy theories","culture",
    "easy listening","economics","education","entertainment","entretenimiento",
    "fashion", "film",
    "football", "government", "humor",
    "information","international", "interviews","literature","live","local","movie", "news","noticias",
    "open air", "podcast",
    "politics","regional","science", "soccer", "social", "soundtrack","speech","sport",
    "storytelling","talk","travel", "tv")


val tagsListByTalk = setOf(
    TagWithGenre.Tag("comedy"),
    TagWithGenre.Tag("commercial"),
    TagWithGenre.Tag("community"),
    TagWithGenre.Tag("conspiracy theories"),
    TagWithGenre.Tag("culture"),
    TagWithGenre.Tag("easy listening"),
    TagWithGenre.Tag("economics"),
    TagWithGenre.Tag("education"),
    TagWithGenre.Tag("entertainment"),
    TagWithGenre.Tag("entretenimiento"),
    TagWithGenre.Tag("fashion"),
    TagWithGenre.Tag("film"),
    TagWithGenre.Tag("football"),
    TagWithGenre.Tag("government"),
    TagWithGenre.Tag("humor"),
    TagWithGenre.Tag("information"),
    TagWithGenre.Tag("international"),
    TagWithGenre.Tag("interviews"),
    TagWithGenre.Tag("literature"),
    TagWithGenre.Tag("live"),
    TagWithGenre.Tag("local"),
    TagWithGenre.Tag("movie"),
    TagWithGenre.Tag("news"),
    TagWithGenre.Tag("noticias"),
    TagWithGenre.Tag("open air"),
    TagWithGenre.Tag("podcast"),
    TagWithGenre.Tag("politics"),
    TagWithGenre.Tag("regional"),
    TagWithGenre.Tag("science"),
    TagWithGenre.Tag("soccer"),
    TagWithGenre.Tag("social"),
    TagWithGenre.Tag("soundtrack"),
    TagWithGenre.Tag("speech"),
    TagWithGenre.Tag("sport"),
    TagWithGenre.Tag("storytelling"),
    TagWithGenre.Tag("talk"),
    TagWithGenre.Tag("travel"),
    TagWithGenre.Tag("tv")
)


val religionTagsSimple = setOf("bible","catholic","christian","christmas","gospel","islamic", "religion","religious")

val tagsListReligion = setOf(
    TagWithGenre.Tag("bible"),
    TagWithGenre.Tag("catholic"),
    TagWithGenre.Tag("christian"),
    TagWithGenre.Tag("christmas"),
    TagWithGenre.Tag("gospel"),
    TagWithGenre.Tag("islamic"),
    TagWithGenre.Tag("religion"),
    TagWithGenre.Tag("religious")
)


val otherTagsSimple = setOf("children", "disney", "juvenil", "kids",
    "grupera", "grupero", "moi merino",  "singer-songwriter", "student", "university")

val tagsListOther = setOf(
    TagWithGenre.Tag("children"),
    TagWithGenre.Tag("disney"),
    TagWithGenre.Tag("juvenil"),
    TagWithGenre.Tag("kids"),
    TagWithGenre.Tag("grupera"),
    TagWithGenre.Tag("grupero"),
    TagWithGenre.Tag("moi merino"),
    TagWithGenre.Tag("singer-songwriter"),
    TagWithGenre.Tag("student"),
    TagWithGenre.Tag("university")
)



