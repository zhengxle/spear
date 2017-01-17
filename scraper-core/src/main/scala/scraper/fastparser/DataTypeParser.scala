package scraper.fastparser

import fastparse.all._

import scraper.annotations.ExtendedSQLSyntax
import scraper.types._

object DataTypeParser extends LoggingParser {
  import KeywordParser._
  import NameParser._
  import WhitespaceApi._

  @ExtendedSQLSyntax
  private val extendedNumericType: P[DataType] =
    TINYINT attach ByteType opaque "extended-type"

  private val exactNumericType: P[DataType] = (
    BIGINT.attach(LongType)
    | INT.attach(IntType)
    | INTEGER.attach(IntType)
    | SMALLINT.attach(ShortType)
    | extendedNumericType
    opaque "exact-numeric-type"
  )

  private val approximateNumericType: P[DataType] = (
    FLOAT.attach(FloatType)
    | REAL.attach(DoubleType)
    | DOUBLE.attach(DoubleType)
    opaque "approximate-numeric-type"
  )

  private val numericType: P[DataType] = (
    exactNumericType
    | approximateNumericType
    opaque "numeric-type"
  )

  private val booleanType: P[DataType] = BOOLEAN attach BooleanType opaque "boolean-type"

  private val datetimeType: P[DataType] = DATE attach DateType opaque "datetime-type"

  @ExtendedSQLSyntax
  private val stringType: P[DataType] = STRING attach StringType opaque "string-type"

  private val characterStringType: P[DataType] = stringType opaque "character-string-type"

  private val predefinedType: P[DataType] = (
    numericType
    | booleanType
    | datetimeType
    | characterStringType
    opaque "predefined-type"
  )

  private val fieldDefinition: P[StructField] = (
    fieldName ~ P(dataType)
    map { case (name, fieldType) => StructField(name, fieldType.?) }
    opaque "field-definition"
  )

  private val rowTypeBody: P[StructType] = (
    "(" ~ fieldDefinition.rep(min = 1, sep = ",") ~ ")"
    map { StructType.apply }
    opaque "row-type-body"
  )

  private val rowType: P[StructType] =
    ROW ~ rowTypeBody opaque "row-type"

  lazy val dataType: P[DataType] =
    predefinedType | rowType opaque "data-type"
}
