package com.cubeguide

import java.io.File
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import org.junit.Test
import kotlin.test.*

class UiEncodingTest {
 @Test fun sourceFilesAreUtf8AndContainNoCorruptedTextMarkers() {
  val files=File("src/main/java").walkTopDown().filter { it.isFile && it.extension=="kt" }.toList()
  assertTrue(files.isNotEmpty())
  for(file in files) {
   val text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(file.readBytes())).toString()
   assertFalse(text.any { it=='\u00c3' || it=='\u00c2' || it=='\ufffd' },file.path)
  }
 }
}
