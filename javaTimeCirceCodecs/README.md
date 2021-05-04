# Java Time Circe Codecs

Custom circe codecs for java time 8.

Based on two concepts:
- a date-time object always has 3 fractional digits
- a date-time object is always formatted in UTC ISO-8601


Uses custom `DateTimeFormatterBuilder`:
```
private[this] val dateTimeFormatterBuilder = new DateTimeFormatterBuilder
//Always have 3 fractional digits
//.appendInstant converts to a data-time with a zone-offset of UTC formatted as ISO-8601
private[this] val dateTimeFormatter = dateTimeFormatterBuilder.appendInstant(3).toFormatter
```
