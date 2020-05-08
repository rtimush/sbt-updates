# Contributing to sbt-updates

Thank you for your willingness to contribute!

## Contributing Code

 * Please consider creating an [issue](https://github.com/rtimush/sbt-updates/issues) first, especially for bigger changes.
 * Make sure that your code is formatted with `scalafmt` and passes the tests.
 * When creating a pull request please make your commits meaningful in isolation. Don't hesitate to amend commits on your pull request branches.
 * A good commit message for this repository starts with a capital letter and can be appended to the statement "If applied, this commit will..."
 
The code is built with JDK 8, and will also be tested on the newer JDK versions. 
To format the code use `sbt scalafmtAll scalafmtSbt`. To run the tests use `sbt test scripted`.
