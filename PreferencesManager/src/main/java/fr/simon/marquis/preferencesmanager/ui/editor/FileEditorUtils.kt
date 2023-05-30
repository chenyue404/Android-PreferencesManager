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

private val COMMENT_END = Pattern.compile("-->")
private val COMMENT_START = Pattern.compile("<!--")
private val TAG_ATTRIBUTE_NAME = Pattern.compile("\\s(\\w*)\\=")
private val TAG_ATTRIBUTE_VALUE = Pattern.compile("[a-z\\-]*\\=(\"[^\"]*\")")
private val TAG_ATTRIBUTE_VALUE_2 = Pattern.compile("[a-z\\-]*\\=(\'[^\']*\')")
private val TAG_END = Pattern.compile("\\??/?>")
private val TAG_START = Pattern.compile("</?[-\\w\\?]+", Pattern.CASE_INSENSITIVE)

class XmlTransformation(private val theme: XmlColorTheme) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder()

        builder.run {
            generateSpan(TAG_START.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.TAG)),
                    start = start,
                    end = end
                )
            }
            generateSpan(TAG_END.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.TAG)),
                    start = start,
                    end = end
                )
            }
            generateSpan(TAG_ATTRIBUTE_VALUE.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.ATTR_VALUE)),
                    start = start,
                    end = end
                )
            }
            generateSpan(TAG_ATTRIBUTE_VALUE_2.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.ATTR_VALUE)),
                    start = start,
                    end = end
                )
            }
            generateSpan(TAG_ATTRIBUTE_NAME.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.ATTR_NAME)),
                    start = start,
                    end = end
                )
            }
            generateSpan(COMMENT_START.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.COMMENT)),
                    start = start,
                    end = end
                )
            }
            generateSpan(COMMENT_END.matcher(text)) { start, end ->
                addStyle(
                    style = SpanStyle(color = theme.getColor(ColorTagEnum.COMMENT)),
                    start = start,
                    end = end
                )
            }

            append(text)
        }

        return TransformedText(
            builder.toAnnotatedString(),
            OffsetMapping.Identity
        )
    }
}

fun generateSpan(matcher: Matcher, applySpan: (start: Int, end: Int) -> Unit) {
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
