/*
 *
 *  Copyright (c) 2021. Mark Grechanik and Lone Star Consulting, Inc. All rights reserved.
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under
 *   the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *   either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package vvakic2.uic.cs441
package Generator

import Generation.RSGStateMachine.{RSGFunction, unit}
import Generation.RandomStringGenerator
import HelperUtils.Parameters
import com.mifmif.common.regex.Generex
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.deprecated.symbolLiterals

class RandomStringGeneratorTest extends AnyFlatSpec with Matchers with PrivateMethodTester {
  behavior of "random string generation"

  private val minStringLength = 1
  private val maxStringLength = 10
  private val randomSeed      = 1

  val INITSTRING        = "Starting the string generation"
  val init: RSGFunction = unit(INITSTRING)
  val rsg: RandomStringGenerator =
    RandomStringGenerator((minStringLength, maxStringLength), randomSeed)

  it should "generate a random string whose length is greater than the min length" in {
    val generationStep = init(rsg)
    println(generationStep)
    val x = -2
    if x < 0 then print(-x) else print(x)
    generationStep._2.length shouldBe >=(minStringLength)
  }

  it should "generate two different random strings consecutively" in {
    val generationStep = init(rsg)
    val string1        = generationStep._2
    val string2        = generationStep._1.next._2
    println(string1)
    string1 should not be string2
  }

  it should "locate an instance of the pattern in the generated string" in {
    val patternString    = "([a-c][e-g][0-3]|[A-Z][5-9][f-w]){5,15}"
    val generex: Generex = new Generex(patternString)
    val generationStep   = init(rsg)
    val string1          = generationStep._2
    val string2          = generationStep._1.next._2
    val genString        = string1 + generex.random() + string2
    genString should include regex patternString.r
  }

  it should "return the same input string if the constructed random string is zero length" in {
    val someString     = "someString"
    val rsg            = RandomStringGenerator((1, 2), 1)
    val callConstruct  = PrivateMethod[String]('constructString)
    val result: String = rsg invokePrivate callConstruct(someString, 0)
    result should fullyMatch regex someString
  }

  it should "return a random string whose length is greater or equal to the one of the base string" in {
    val someString     = "someString"
    val rsg            = RandomStringGenerator((1, 10), 1)
    val callConstruct  = PrivateMethod[String]('constructString)
    val result: String = rsg invokePrivate callConstruct(someString, 0)
    result.length shouldBe >=(someString.length)
  }

  it should "return a random string that starts with the base string" in {
    val someString     = "someString"
    val rsg            = RandomStringGenerator((1, 10), 1)
    val callConstruct  = PrivateMethod[String]('constructString)
    val result: String = rsg invokePrivate callConstruct(someString, 10)
    result startsWith someString
  }

}
