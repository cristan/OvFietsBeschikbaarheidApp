import XCTest

final class AppLaunchSmokeTest: XCTestCase {
    func testHomeScreenAppears() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.staticTexts["OV-fiets Beschikbaarheid"].waitForExistence(timeout: 30))
    }
}
