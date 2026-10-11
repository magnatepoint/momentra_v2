import SwiftUI

enum BusinessSetupKind: String, CaseIterable, Identifiable {
    case marketingCampaigns = "MARKETING_CAMPAIGNS"
    case tradingDistribution = "TRADING_DISTRIBUTION"
    case retailConsumer = "RETAIL_CONSUMER"
    case manufacturingProduction = "MANUFACTURING_PRODUCTION"

    var id: String { rawValue }
    var familyCode: String { rawValue }

    var title: String {
        switch self {
        case .marketingCampaigns: return "Marketing & Campaigns"
        case .tradingDistribution: return "Trading & Distribution"
        case .retailConsumer: return "Retail & Consumer"
        case .manufacturingProduction: return "Manufacturing & Production"
        }
    }

    var body: String {
        switch self {
        case .marketingCampaigns: return "Budget, channels, people, and results for a campaign."
        case .tradingDistribution: return "Source, move, and collect across wholesale or distribution."
        case .retailConsumer: return "A store, a food outlet, or a consumer service."
        case .manufacturingProduction: return "Standard production or made-to-order job work."
        }
    }

    var analyticsScreen: String {
        switch self {
        case .marketingCampaigns: return AnalyticsScreens.businessSetupTeamOps
        case .tradingDistribution: return AnalyticsScreens.businessSetupRunway
        case .retailConsumer, .manufacturingProduction: return AnalyticsScreens.businessSetupOps
        }
    }

    var activateColor: Color {
        switch self {
        case .marketingCampaigns: return Color(hex: "#818CF8")
        case .tradingDistribution: return Color(hex: "#FBBF24")
        case .retailConsumer: return Color(hex: "#34D399")
        case .manufacturingProduction: return Color(hex: "#38BDF8")
        }
    }

    var maestroTag: String {
        switch self {
        case .marketingCampaigns: return "business.setup.marketing_campaigns"
        case .tradingDistribution: return "business.setup.trading_distribution"
        case .retailConsumer: return "business.setup.retail_consumer"
        case .manufacturingProduction: return "business.setup.manufacturing_production"
        }
    }

    var ctaGradient: LinearGradient {
        LinearGradient(colors: [activateColor, activateColor.opacity(0.8)], startPoint: .leading, endPoint: .trailing)
    }
}
