package com.vahitkeskin.loopsweep.data.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VacuumClientTest {

    @Test
    fun testParseRoomsFromGetMap_escapedJson() {
        val json = """{"id":105,"result":[{"siid":12,"piid":4,"code":0,"value":"[{\"name\":\"Tüm ev\",\"id\":1763994619},{\"name\":\"Balkon\",\"id\":1779096923}]"}]}"""
        val rooms = VacuumClient.parseRoomsFromGetMap(json)
        
        assertEquals(2, rooms.size)
        assertEquals(1763994619L, rooms[0].first)
        assertEquals("Tüm ev", rooms[0].second)
        
        assertEquals(1779096923L, rooms[1].first)
        assertEquals("Balkon", rooms[1].second)
    }

    @Test
    fun testParseRoomsFromGetMap_unescapedJson() {
        val json = """[{"name":"Tüm ev","id":1763994619},{"name":"Balkon","id":1779096923}]"""
        val rooms = VacuumClient.parseRoomsFromGetMap(json)
        
        assertEquals(2, rooms.size)
        assertEquals(1763994619L, rooms[0].first)
        assertEquals("Tüm ev", rooms[0].second)
        
        assertEquals(1779096923L, rooms[1].first)
        assertEquals("Balkon", rooms[1].second)
    }

    @Test
    fun testParseRoomsFromGetMap_emptyOrInvalid() {
        val roomsEmpty = VacuumClient.parseRoomsFromGetMap("")
        assertTrue(roomsEmpty.isEmpty())

        val roomsInvalid = VacuumClient.parseRoomsFromGetMap("""{"error": "something"}""")
        assertTrue(roomsInvalid.isEmpty())
    }
}
