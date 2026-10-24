import CoreLocation
import XCTest

final class AppLaunchSmokeTest: XCTestCase {
    func testNearbyLocationsLoadAfterAllowingLocation() {
        XCUIDevice.shared.location = XCUILocation(location: CLLocation(latitude: 52.3791, longitude: 4.9003))
        let app = XCUIApplication()
        app.resetAuthorizationStatus(for: .location)
        app.launch()

        app.buttons["OV-fiets locaties in je buurt"].tap()

        let allowButton = XCUIApplication(bundleIdentifier: "com.apple.springboard").alerts.buttons["Allow While Using App"]
        XCTAssertTrue(allowButton.waitForExistence(timeout: 10))
        allowButton.tap()

        XCTAssertTrue(app.staticTexts["Bij jou in de buurt"].waitForExistence(timeout: 30))
        XCTAssertFalse(app.wait(for: .notRunning, timeout: 5))

        let locationRow = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "OV-fietsen beschikbaar")).firstMatch
        XCTAssertTrue(locationRow.waitForExistence(timeout: 30))
        let alignmentPlaceholder = app.descendants(matching: .any).matching(NSPredicate(format: "label CONTAINS %@", "888")).firstMatch
        XCTAssertFalse(alignmentPlaceholder.exists)
    }
}
