package com.example.radioplayer.utils

import com.example.radioplayer.adapters.models.TagWithGenre

const val TAG_BY_PERIOD = "-BY PERIOD-"
const val TAG_BY_GENRE = "-POPULAR GENRES-"
const val TAG_BY_MINDFUL = "-MINDFULNESS-"
const val TAG_BY_ORIGIN = "-REGIONAL-"
const val TAG_BY_TALK = "-TALK, NEWS, etc-"
const val TAG_BY_RELIGION = "-RELIGION-"
const val TAG_BY_OTHER = "-OTHER-"

const val TAG_BY_CLASSIC = "-ART, CLASSICAL-"
const val TAG_BY_EXPERIMENTAL = "-EXPERIMENTAL-"
const val TAG_BY_SPECIAL = "-SPECIAL-"

val listOfTagsSimple = listOf("charts", "hits", "top")

val tagsListSpecial = setOf(
    TagWithGenre.Tag("charts"),
    TagWithGenre.Tag("classic hits"),
    TagWithGenre.Tag("contemporary hits"),
    TagWithGenre.Tag("dj"),
    TagWithGenre.Tag("freeform"),
    TagWithGenre.Tag("hits"),
    TagWithGenre.Tag("mix"),
    TagWithGenre.Tag("music for study"),
    TagWithGenre.Tag("no ads"),
    TagWithGenre.Tag("top"),
    TagWithGenre.Tag("workout")

)


val byPeriodTagsSimple = setOf("30s", "40s",
"50s", "60s", "70s", "80s", "90s", "00s", "10s", "decades","golden", "goldies","medieval","nostalgia",
"oldies", "oldschool", "old time radio", "retro")

val tagsListByPeriod = setOf(
    TagWithGenre.Tag("30s"), TagWithGenre.Tag("40s"), TagWithGenre.Tag("50s"),
    TagWithGenre.Tag("60s"), TagWithGenre.Tag("70s"), TagWithGenre.Tag("80s"),
    TagWithGenre.Tag("90s"), TagWithGenre.Tag("00s"), TagWithGenre.Tag("10s"),
    TagWithGenre.Tag("decades"), TagWithGenre.Tag("golden"),
    TagWithGenre.Tag("goldies"),
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
    "new age","new wave", "noise", "opera","orchestral","organ", "party","patriot", "piano","pop",
    "progressive", "psy", "punk","r&b","rap","reggae","remixes","rnb","rock","romance",
    "romantic","romántica","salsa",
    "schlager", "surf music", "synth", "symphony", "swing","techno",


)


val tagsListByGenre = setOf(
    TagWithGenre.Tag("acoustic"),
    TagWithGenre.Tag("adult contemporary"),
    TagWithGenre.Tag("adult album alternative"),
    TagWithGenre.Tag("alternative"),
    TagWithGenre.Tag("bass"),
    TagWithGenre.Tag("beat"),
    TagWithGenre.Tag("black metal"),
    TagWithGenre.Tag("blues"),
    TagWithGenre.Tag("boogie"),
    TagWithGenre.Tag("classic rock"),
    TagWithGenre.Tag("club"),
    TagWithGenre.Tag("conservative"),
    TagWithGenre.Tag("country"),
    TagWithGenre.Tag("covers"),
    TagWithGenre.Tag("dance"),
    TagWithGenre.Tag("death metal"),
    TagWithGenre.Tag("disco"),
    TagWithGenre.Tag("drum and bass"),
    TagWithGenre.Tag("dubstep"),
    TagWithGenre.Tag("eclectic"),
    TagWithGenre.Tag("edm"),
    TagWithGenre.Tag("electro"),
    TagWithGenre.Tag("folk"),
    TagWithGenre.Tag("funk"),
    TagWithGenre.Tag("garage"),
    TagWithGenre.Tag("groove"),
    TagWithGenre.Tag("guitar"),
    TagWithGenre.Tag("hardcore"),
    TagWithGenre.Tag("hardstyle"),
    TagWithGenre.Tag("heavy metal"),
    TagWithGenre.Tag("hip-hop"),
    TagWithGenre.Tag("hiphop"),
    TagWithGenre.Tag("hot adult contemporary"),
    TagWithGenre.Tag("house"),
    TagWithGenre.Tag("independent"),
    TagWithGenre.Tag("indie"),
    TagWithGenre.Tag("instrumental"),
    TagWithGenre.Tag("jazz"),
    TagWithGenre.Tag("jungle"),
    TagWithGenre.Tag("love"),
    TagWithGenre.Tag("mainstream"),
    TagWithGenre.Tag("metal"),
    TagWithGenre.Tag("new age"),
    TagWithGenre.Tag("new wave"),
    TagWithGenre.Tag("party"),
    TagWithGenre.Tag("pop"),
    TagWithGenre.Tag("punk"),
    TagWithGenre.Tag("r&b"),
    TagWithGenre.Tag("rap"),
    TagWithGenre.Tag("reggae"),
    TagWithGenre.Tag("remixes"),
    TagWithGenre.Tag("rnb"),
    TagWithGenre.Tag("rock"),
    TagWithGenre.Tag("romance"),
    TagWithGenre.Tag("schlager"),
    TagWithGenre.Tag("soft adult contemporary"),
    TagWithGenre.Tag("soft rock"),
    TagWithGenre.Tag("synth"),
    TagWithGenre.Tag("swing"),
    TagWithGenre.Tag("techno"),
    TagWithGenre.Tag("urban"),
    TagWithGenre.Tag("vocal")
)

val tagsListExperimental = setOf(
    TagWithGenre.Tag("avant-garde"),
    TagWithGenre.Tag("experimental"),
    TagWithGenre.Tag("gothic"),
    TagWithGenre.Tag("industrial"),
    TagWithGenre.Tag("lo-fi"),
    TagWithGenre.Tag("noise"),
    TagWithGenre.Tag("progressive"),
    TagWithGenre.Tag("psychedelic"),
    TagWithGenre.Tag("psychill"),
    TagWithGenre.Tag("psytrance"),
    TagWithGenre.Tag("trance"),
    TagWithGenre.Tag("trip hop"),
    TagWithGenre.Tag("underground")
)

val tagsListClassics = setOf(
    TagWithGenre.Tag("contemporary classical"),
    TagWithGenre.Tag("drama"),
    TagWithGenre.Tag("medieval"),
    TagWithGenre.Tag("modern classical"),
    TagWithGenre.Tag("classical"),
    TagWithGenre.Tag("classical baroque"),
    TagWithGenre.Tag("classical piano"),
    TagWithGenre.Tag("opera"),
    TagWithGenre.Tag("orchestral"),
    TagWithGenre.Tag("organ"),
    TagWithGenre.Tag("piano"),
    TagWithGenre.Tag("musica clasica romantica"),
    TagWithGenre.Tag("symphony"),
    TagWithGenre.Tag("symphonic metal"),

)


val specialTagsSimple = setOf("amor","atmospheric", "calm", "chill","downtempo", "fantasy", "flac", "love", "meditation", "multicultural",
"music for study", "nature", "no ads", "paranormal", "relax",
"sleep", "slow", "smooth", "soft",  "soul", "spiritual","workout")

val tagsListMindful = setOf(
    TagWithGenre.Tag("ambient"),
    TagWithGenre.Tag("atmospheric"),
    TagWithGenre.Tag("calm"),
    TagWithGenre.Tag("chill"),
    TagWithGenre.Tag("downtempo"),
    TagWithGenre.Tag("easy listening"),
    TagWithGenre.Tag("fantasy"),
    TagWithGenre.Tag("flac"),
    TagWithGenre.Tag("lounge"),
    TagWithGenre.Tag("meditation"),
    TagWithGenre.Tag("nature"),
    TagWithGenre.Tag("paranormal"),
    TagWithGenre.Tag("relax"),
    TagWithGenre.Tag("sleep"),
    TagWithGenre.Tag("slow"),
    TagWithGenre.Tag("smooth"),
    TagWithGenre.Tag("soft music"),
    TagWithGenre.Tag("soul"),
    TagWithGenre.Tag("spiritual"),

)



val byOriginTagsSimple = setOf("aboriginal", "african", "afrobeats", "americana", "arabic", "asian",  "banda","bollywood",
    "caribbean", "celtic", "ethnic","greek", "grupera", "grupero", "indian music",
    "latin", "méxico", "national", "spanish")


val tagsListByOrigin = setOf(
    TagWithGenre.Tag("aboriginal"),
    TagWithGenre.Tag("african"),
    TagWithGenre.Tag("afrobeats"),
    TagWithGenre.Tag("americana"),
    TagWithGenre.Tag("amor"),
    TagWithGenre.Tag("arabic"),
    TagWithGenre.Tag("asian"),
    TagWithGenre.Tag("bachata"),
    TagWithGenre.Tag("balada"),
    TagWithGenre.Tag("banda"),
    TagWithGenre.Tag("bollywood"),
    TagWithGenre.Tag("caribbean"),
    TagWithGenre.Tag("celtic"),
    TagWithGenre.Tag("ethnic"),
    TagWithGenre.Tag("flamenco"),
    TagWithGenre.Tag("greek"),
    TagWithGenre.Tag("grupera"),
    TagWithGenre.Tag("grupero"),
    TagWithGenre.Tag("indian music"),
    TagWithGenre.Tag("latin"),
    TagWithGenre.Tag("méxico"),
    TagWithGenre.Tag("moi merino"),
    TagWithGenre.Tag("regional"),
    TagWithGenre.Tag("romántica"),
    TagWithGenre.Tag("salsa"),
    TagWithGenre.Tag("spanish"),
    TagWithGenre.Tag("traditional"),
    TagWithGenre.Tag("tropical"),
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
    TagWithGenre.Tag("science"),
    TagWithGenre.Tag("soccer"),
    TagWithGenre.Tag("social"),
    TagWithGenre.Tag("soundtrack"),
    TagWithGenre.Tag("speech"),
    TagWithGenre.Tag("sport"),
    TagWithGenre.Tag("storytelling"),
    TagWithGenre.Tag("talk"),
    TagWithGenre.Tag("traffic"),
    TagWithGenre.Tag("travel"),
    TagWithGenre.Tag("tv")
)


val religionTagsSimple = setOf("bible","catholic","christian","christmas","gospel","islamic", "religion","religious")

val tagsListReligion = setOf(
    TagWithGenre.Tag("christian contemporary"),
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
     "moi merino",  "singer-songwriter", "student", "university")

val tagsListOther = setOf(
    TagWithGenre.Tag("anime"),
    TagWithGenre.Tag("children"),
    TagWithGenre.Tag("disney"),
    TagWithGenre.Tag("juvenil"),
    TagWithGenre.Tag("kids"),
    TagWithGenre.Tag("lifestyle"),
    TagWithGenre.Tag("multicultural"),
    TagWithGenre.Tag("national"),
    TagWithGenre.Tag("patriot"),
    TagWithGenre.Tag("singer-songwriter"),
    TagWithGenre.Tag("student"),
    TagWithGenre.Tag("surf music"),
    TagWithGenre.Tag("university")
)



