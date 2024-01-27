@file:Suppress("RegExpRedundantEscape")

package fr.simon.marquis.preferencesmanager.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme.ColorTagEnum
import java.util.regex.Matcher
import java.util.regex.Pattern

private val commentEnd = Pattern.compile("-->")
private val commentStart = Pattern.compile("<!--")
private val tagAttributeName = Pattern.compile("\\s(\\w*)\\=")
private val tagAttributeValue = Pattern.compile("[a-z\\-]*\\=(\"[^\"]*\")")
private val tagAttributeValue2 = Pattern.compile("[a-z\\-]*\\=(\'[^\']*\')")
private val tagEnd = Pattern.compile("\\??/?>")
private val tagStart = Pattern.compile("</?[-\\w\\?]+", Pattern.CASE_INSENSITIVE)

class XmlTransformation(private val theme: XmlColorTheme) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder()

        with(builder) {
            generateSpan(tagStart.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.TAG)),
                    start = start,
                    end = end
                )
            }
            generateSpan(tagEnd.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.TAG)),
                    start = start,
                    end = end
                )
            }
            generateSpan(tagAttributeValue.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.ATTR_VALUE)),
                    start = start,
                    end = end
                )
            }
            generateSpan(tagAttributeValue2.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.ATTR_VALUE)),
                    start = start,
                    end = end
                )
            }
            generateSpan(tagAttributeName.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.ATTR_NAME)),
                    start = start,
                    end = end
                )
            }
            generateSpan(commentStart.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.COMMENT)),
                    start = start,
                    end = end
                )
            }
            generateSpan(commentEnd.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.COMMENT)),
                    start = start,
                    end = end
                )
            }

            append(text)
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

private fun generateSpan(matcher: Matcher, applySpan: (start: Int, end: Int) -> Unit) {
    var start: Int
    var end: Int
    while (matcher.find()) {
        start = matcher.start()
        end = matcher.end()
        if (start != end) {
            applySpan(start, end)
        }
    }
}
