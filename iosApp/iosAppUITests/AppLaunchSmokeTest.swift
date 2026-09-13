import XCTest

final class AppLaunchSmokeTest: XCTestCase {
    func testNearbyLocationsLoadAfterAllowingLocation() {
        let app = XCUIApplication()
        app.resetAuthorizationStatus(for: .location)
        app.launch()

        app.buttons["OV-fiets locaties in je buurt"].tap()

        let allowButton = XCUIApplication(bundleIdentifier: "com.apple.springboard").alerts.buttons["Allow While Using App"]
        XCTAssertTrue(allowButton.waitForExistence(timeout: 10))
        allowButton.tap()

        XCTAssertTrue(app.staticTexts["Bij jou in de buurt"].waitForExistence(timeout: 30))
        XCTAssertFalse(app.wait(for: .notRunning, timeout: 5))
    }
}
