package com.example.todolist.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.SemanticsMatcher
import org.junit.Rule
import org.junit.Test

class BottomNavigationAccessibilityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val selectedMatcher = SemanticsMatcher.expectValue(
        SemanticsProperties.StateDescription,
        "선택됨"
    )

    @Test
    fun basicTabLabels_areExposed() {
        composeRule.setContent {
            MaterialTheme {
                BottomNavigationSection(
                    selectedTab = TodoMainTab.TODO,
                    overdueActiveCount = 0,
                    onTabSelected = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("할 일").assertExists()
        composeRule.onNodeWithContentDescription("달력").assertExists()
        composeRule.onNodeWithContentDescription("검색").assertExists()
    }

    @Test
    fun selectedTab_hasStateDescription_selected() {
        composeRule.setContent {
            MaterialTheme {
                BottomNavigationSection(
                    selectedTab = TodoMainTab.CALENDAR,
                    overdueActiveCount = 0,
                    onTabSelected = {}
                )
            }
        }

        composeRule.onNode(hasContentDescription("달력").and(selectedMatcher)).assertExists()
        composeRule.onNode(hasContentDescription("할 일").and(selectedMatcher)).assertDoesNotExist()
        composeRule.onNode(hasContentDescription("검색").and(selectedMatcher)).assertDoesNotExist()
    }

    @Test
    fun todoTab_withOverdueCount_showsA11yDescriptionAndBadgeText() {
        composeRule.setContent {
            MaterialTheme {
                BottomNavigationSection(
                    selectedTab = TodoMainTab.TODO,
                    overdueActiveCount = 3,
                    onTabSelected = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("할 일, 지난 일정 3개 있음").assertExists()
        composeRule.onNodeWithText("3").assertExists()
    }

    @Test
    fun todoTab_withLargeOverdueCount_showsTenPlusA11yDescription_andNinePlusBadge() {
        composeRule.setContent {
            MaterialTheme {
                BottomNavigationSection(
                    selectedTab = TodoMainTab.TODO,
                    overdueActiveCount = 12,
                    onTabSelected = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("할 일, 지난 일정 10개 이상 있음").assertExists()
        composeRule.onNodeWithText("9+").assertExists()
    }

    @Test
    fun todoTab_withNoOverdueCount_doesNotIncludeOverduePhrase() {
        composeRule.setContent {
            MaterialTheme {
                BottomNavigationSection(
                    selectedTab = TodoMainTab.TODO,
                    overdueActiveCount = 0,
                    onTabSelected = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("할 일").assertExists()
        composeRule.onNodeWithContentDescription("할 일, 지난 일정 1개 있음").assertDoesNotExist()
    }
}
