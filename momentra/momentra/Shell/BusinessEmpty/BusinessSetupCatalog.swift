import Foundation

struct BusinessSetupTextFieldSpec {
    let key: String
    let label: String
}

struct BusinessSetupSectionSpec {
    let title: String
    let fields: [PersonalSetupFieldSpec]
    var textFields: [BusinessSetupTextFieldSpec] = []
}

struct BusinessSetupCatalogEntry {
    let defaultTitle: String
    let subtitle: String
    let momentTypeCode: String
    let activateLabel: String
    let footerTagline: String
    let sections: [BusinessSetupSectionSpec]
    let fields: [PersonalSetupFieldSpec]
    let defaultPreferences: [String: Any]
}

enum BusinessSetupCatalog {
    static func forKind(_ kind: BusinessSetupKind) -> BusinessSetupCatalogEntry {
        let subtype = OperatingMomentCatalog.subtypes(kind)[0]
        return BusinessSetupCatalogEntry(
            defaultTitle: kind.title,
            subtitle: kind.body,
            momentTypeCode: subtype.code,
            activateLabel: "Activate",
            footerTagline: kind.body,
            sections: [],
            fields: [],
            defaultPreferences: [
                "subtype": subtype.code,
                "identityName": "",
                "partyName": "",
                "description": "",
                "currency": "INR",
                "startDate": "",
                "participants": [String: String](),
                "counterparties": [String: String](),
                "channels": [String](),
                "monthlyBudget": "",
                "monthlyTarget": "",
                "targetMetric": subtype.targetMetrics[0],
            ]
        )
    }

    static func footerTagline(kind: BusinessSetupKind, audience: String) -> String {
        kind.body
    }

    static func defaultPreferences(kind: BusinessSetupKind, audience: String) -> [String: Any] {
        forKind(kind).defaultPreferences
    }
}
