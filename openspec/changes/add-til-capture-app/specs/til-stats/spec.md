## Purpose

Turns the pile of entries into evidence of a habit: which days were hit, how long the current run is, and how much was learned this week and this month.

## ADDED Requirements

### Requirement: Calendar streak view

The system SHALL present a month-by-month calendar in which days holding at least one entry are marked distinctly from days holding none, and SHALL let the user move between months. Days SHALL be assigned to dates using the device's local timezone.

#### Scenario: Days with entries are marked

- **WHEN** a month is displayed in the calendar
- **THEN** every day on which at least one entry was captured is visually marked
- **AND** days with no entries are visually unmarked

#### Scenario: Busier days read as busier

- **WHEN** one day holds several entries and another holds exactly one
- **THEN** the day with more entries is marked with greater visual weight than the day with one

#### Scenario: Moving between months

- **WHEN** a user navigates to the previous or next month
- **THEN** the calendar displays that month's days and marks them from that month's entries

#### Scenario: Future days are inert

- **WHEN** the displayed month contains dates later than today
- **THEN** those dates are shown as inactive and are never marked

### Requirement: Current and longest streak

The system SHALL report the current streak and the longest streak ever achieved, where a streak is a run of consecutive calendar days each holding at least one entry. The current streak SHALL count a run ending today, or a run ending yesterday when today holds no entry yet, so that an unbroken habit is not reported as zero before the day's entry is made.

#### Scenario: Streak ending today

- **WHEN** entries exist on each of the last four consecutive days including today
- **THEN** the current streak is reported as four days

#### Scenario: Today not yet captured

- **WHEN** entries exist on each of the three days ending yesterday and today holds no entry
- **THEN** the current streak is reported as three days

#### Scenario: Streak broken by a missed day

- **WHEN** the most recent entry is three days old and the two days since hold no entries
- **THEN** the current streak is reported as zero

#### Scenario: Multiple entries in a day count once

- **WHEN** a day within a streak holds five entries
- **THEN** that day contributes one day to the streak length

#### Scenario: Longest streak is retained

- **WHEN** a past run of days is longer than the current run
- **THEN** the longest streak reports that past run's length
- **AND** it is unaffected by the current streak being broken

### Requirement: Weekly and monthly summaries

The system SHALL report, for the current week and the current calendar month, how many entries were captured and on how many distinct days. Week boundaries SHALL follow the device locale's first day of week.

#### Scenario: Weekly summary

- **WHEN** the statistics view is opened
- **THEN** it reports the number of entries captured in the current week and the number of distinct days in that week holding an entry

#### Scenario: Monthly summary

- **WHEN** the statistics view is opened
- **THEN** it reports the number of entries captured in the current calendar month and the number of distinct days in that month holding an entry

#### Scenario: Week rollover

- **WHEN** a new week begins
- **THEN** the weekly summary counts only entries from the new week and excludes the previous week's

### Requirement: Statistics stay accurate

Reported statistics SHALL reflect the current set of entries, updating when entries are added, edited, or deleted, and SHALL render meaningfully when there are no entries at all.

#### Scenario: A new entry updates the statistics

- **WHEN** an entry is captured on a day that previously held none
- **THEN** that day becomes marked in the calendar and the weekly and monthly counts increase accordingly

#### Scenario: A deletion updates the statistics

- **WHEN** the only entry on a given day is deleted
- **THEN** that day is no longer marked and the counts covering it decrease accordingly
- **AND** a streak that depended on that day is recalculated

#### Scenario: No entries at all

- **WHEN** the statistics view is opened and no entries exist
- **THEN** streaks and counts are reported as zero alongside an inviting message
- **AND** the calendar renders the current month with no days marked
