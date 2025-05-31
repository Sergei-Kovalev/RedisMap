package org.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RedisMapTest {

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:8.0.2")
            .withExposedPorts(6379);

    private RedisMap redisMap;

    @BeforeEach
    void setUp() {
        String host = redis.getHost();
        int port = redis.getFirstMappedPort();
        redisMap = new RedisMap(host, port);
    }

    @Nested
    class SizeTests {
        @Test
        void size_whenEmpty() {
            assertEquals(0, redisMap.size());
        }

        @Test
        void size_whenElementsExists() {
            redisMap.put("Hi", "Man");
            assertEquals(1, redisMap.size());
        }
    }

    @Nested
    class IsEmptyTests {
        @Test
        void isEmpty_whenEmpty() {
            assertTrue(redisMap.isEmpty());
        }

        @Test
        void isEmpty_whenNotEmpty() {
            redisMap.put("Hi", "Man");
            assertFalse(redisMap.isEmpty());
        }
    }

    @Nested
    class ContainsKeyTests {
        @Test
        void containsKey_whenKeyIsNull() {
            assertFalse(redisMap.containsKey(null));
        }

        @Test
        void containsKey_whenKeyNotPresent() {
            assertFalse(redisMap.containsKey("Hi"));
        }

        @Test
        void containsKey_whenKeyIsPresent() {
            redisMap.put("Hi", "Man");
            assertTrue(redisMap.containsKey("Hi"));
        }
    }

    @Nested
    class ContainsValueTests {
        @Test
        void containsValue_whenValueIsNull() {
            assertFalse(redisMap.containsValue(null));
        }

        @Test
        void containsValue_whenValueNotPresent() {
            assertFalse(redisMap.containsValue("Man"));
        }

        @Test
        void containsValue_whenValueIsPresent() {
            redisMap.put("Hi", "Man");
            assertTrue(redisMap.containsValue("Man"));
        }
    }

    @Nested
    class GetTests {
        @Test
        void get_whenKeyIsNull() {
            assertNull(redisMap.get(null));
        }

        @Test
        void get_whenKeyNotPresent() {
            assertNull(redisMap.get("Hi"));
        }

        @Test
        void get_whenKeyIsPresent() {
            redisMap.put("Hi", "Man");
            assertEquals("Man", redisMap.get("Hi"));
        }
    }

    @Nested
    class PutTests {
        @Test
        void put_whenKeyOrValueIsNull() {
            assertThrows(NullPointerException.class, () -> redisMap.put(null, null));
            assertThrows(NullPointerException.class, () -> redisMap.put("Hi", null));
            assertThrows(NullPointerException.class, () -> redisMap.put(null, "Man"));
        }

        @Test
        void put_whenKeyValueNotPresent() {
            assertNull(redisMap.put("Hi", "Man"));
        }

        @Test
        void put_whenKeyValueIsPresent() {
            redisMap.put("Hi", "Man");
            assertEquals("Man", redisMap.put("Hi", "Woman"));
        }
    }

    @Nested
    class RemoveTests {
        @Test
        void remove_whenKeyIsNull() {
            assertNull(redisMap.remove(null));
        }

        @Test
        void remove_whenKeyNotPresent() {
            assertNull(redisMap.remove("Hi"));
        }

        @Test
        void remove_whenKeyIsPresent() {
            redisMap.put("Hi", "Man");
            assertEquals("Man", redisMap.remove("Hi"));
        }
    }

    @Nested
    class PutAllTests {
        @Test
        void putAll_whenMapIsNull() {
            assertDoesNotThrow(() -> redisMap.putAll(null));
            assertTrue(redisMap.isEmpty(), "После putAll с null ничего не должно быть записано");
        }

        @ParameterizedTest
        @MethodSource("provideMapsWithNulls")
        void putAll_whenMapContainsElements_withNull(Map<String, String> map) {
            NullPointerException npe = assertThrows(NullPointerException.class, () -> redisMap.putAll(map));
            assertEquals("Ключ или значение null, не поддерживается Redis.", npe.getMessage());
        }

        static Stream<Map<String, String>> provideMapsWithNulls() {
            Map<String, String> map1 = new HashMap<>();
            map1.put(null, "Man");
            Map<String, String> map2 = new HashMap<>();
            map2.put("Hi", null);
            return Stream.of(map1, map2);
        }

        @Test
        void putAll_whenMapContainsCorrectElements() {
            Map<String, String> map = Map.of("Hi", "Man", "Hello", "Woman");

            redisMap.putAll(map);

            assertEquals(2, redisMap.size());
            assertEquals("Man", redisMap.get("Hi"));
            assertEquals("Woman", redisMap.get("Hello"));
        }
    }

    @Nested
    class ClearTests {
        @Test
        void clear() {
            redisMap.put("Hi", "Man");
            redisMap.clear();
            assertEquals(0, redisMap.size());
        }
    }

    @Nested
    class KeySetTests {
        @Test
        void keySet_whenEmpty() {
            assertEquals(0, redisMap.keySet().size());
        }

        @Test
        void keySet_whenElementsExists() {
            redisMap.put("Hi", "Man");
            redisMap.put("Hello", "Woman");
            assertEquals(2, redisMap.keySet().size());
            assertEquals("Hi", redisMap.keySet().iterator().next());
        }
    }

    @Nested
    class ValueSetTests {
        @Test
        void values_whenEmpty() {
            assertEquals(0, redisMap.values().size());
        }

        @Test
        void values_whenElementsExists() {
            redisMap.put("Hi", "Man");
            redisMap.put("Hello", "Woman");
            assertEquals(2, redisMap.values().size());
            assertEquals("Man", redisMap.values().iterator().next());
        }
    }

    @Nested
    class EntrySetTests {
        @Test
        void entrySet_whenEmpty() {
            assertEquals(0, redisMap.entrySet().size());
        }

        @Test
        void entrySet_whenElementsExists() {
            redisMap.put("Hi", "Man");
            redisMap.put("Hello", "Woman");
            assertEquals(2, redisMap.entrySet().size());
            assertTrue(redisMap.entrySet().stream()
                               .anyMatch(entry -> entry.getKey().equals("Hi") && entry.getValue().equals("Man")));
        }
    }

    @AfterEach
    void tearDown() {
        redisMap.clear();
    }
}