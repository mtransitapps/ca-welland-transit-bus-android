package org.mtransit.parser.ca_welland_transit_bus;

import static org.mtransit.parser.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GAgency;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://niagaraopendata.ca/dataset/niagara-region-transit-gtfs
public class WellandTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new WellandTransitBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Welland Transit";
	}

	@Override
	public boolean excludeAgency(@NotNull GAgency gAgency) {
		//noinspection deprecation
		final String agencyId = gAgency.getAgencyId();
		if (!agencyId.startsWith("WE_") //
				&& !agencyId.startsWith("Wel_") //
				&& !agencyId.startsWith("WEL_") //
				&& !agencyId.contains("AllNRT_")
				&& !agencyId.equals("1")) {
			return EXCLUDE;
		}
		return super.excludeAgency(gAgency);
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		//noinspection deprecation
		final String agencyId = gRoute.getAgencyIdOrDefault();
		if (!agencyId.startsWith("WE_") //
				&& !agencyId.startsWith("Wel_") //
				&& !agencyId.startsWith("WEL_") //
				&& !agencyId.contains("AllNRT_")
				&& !agencyId.equals("1")) {
			return EXCLUDE;
		}
		if (agencyId.contains("AllNRT_") || agencyId.equals("1")) {
			final String rsnS = gRoute.getRouteShortName();
			if (!CharUtils.isDigitsOnly(rsnS)) {
				return EXCLUDE;
			}
			final int rsn = Integer.parseInt(rsnS);
			if (rsn < 500 || rsn > 799) { // includes Port Colborne for now
				return EXCLUDE;
			}
		}
		if (gRoute.getRouteLongNameOrDefault().startsWith("NRT - ")) {
			return EXCLUDE; // Niagara Region Transit buses
		}
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	private static final Pattern POINT = Pattern.compile("((^|\\W)([\\w])\\.(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String POINT_REPLACEMENT = "$2" + "$3" + "$4";

	private static final Pattern POINTS = Pattern.compile("((^|\\W)([\\w]+)\\.(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String POINTS_REPLACEMENT = "$2" + "$3" + "$4";

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = POINT.matcher(routeLongName).replaceAll(POINT_REPLACEMENT);
		routeLongName = POINTS.matcher(routeLongName).replaceAll(POINTS_REPLACEMENT);
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "00AAA0"; // GREEN (from PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		switch (gRoute.getRouteShortName()) {
		// @formatter:off
		case "23": return "2B6ABC";
		case "25": return "9E50AE";
		case "34": return "2B6ABC";
		case "501": return "ED1C24";
		case "502": return "A05843";
		case "503": return "00A990";
		case "504": return "2E3192";
		case "505": return "7B2178";
		case "506": return "19B5F1";
		case "508": return "EC008C";
		case "509": return "127BCA";
		case "599": return null; // TODO
		case "510": return "ED1C24";
		case "511": return "2E3192";
		case "701": return "ED1C24";
		case "702": return "127BCA";
		// @formatter:on
		}
		throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
	}

	private static final Pattern STARTS_WITH_WE_A00_ = Pattern.compile(
			"((^)((allnrt|wel|we)_[a-z]{1,3}[\\d]{2,4}(_)?(stop)?))",
			Pattern.CASE_INSENSITIVE
	);

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = STARTS_WITH_WE_A00_.matcher(gStopId).replaceAll(EMPTY);
		return gStopId;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(getFirstLanguageNN(), tripHeadsign);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH = Pattern.compile("(([&/\\-])[\\W]*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_FLAG_STOP = Pattern.compile("(^(flag stop - )+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern FIX_AVENUE = Pattern.compile("((^|\\W)(aven|avenu)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String FIX_AVENUE_REPLACEMENT = "$2" + "Avenue" + "$4";

	private static final Pattern FIX_DRIVE = Pattern.compile("((^|\\W)(driv)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String FIX_DRIVE_REPLACEMENT = "$2" + "Drive" + "$4";

	private static final Pattern FIX_STREET = Pattern.compile("((^|\\W)(stree|stre)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String FIX_STREET_REPLACEMENT = "$2" + "Street" + "$4";

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(getFirstLanguageNN(), gStopName);
		gStopName = STARTS_WITH_FLAG_STOP.matcher(gStopName).replaceAll(EMPTY);
		gStopName = FIX_AVENUE.matcher(gStopName).replaceAll(FIX_AVENUE_REPLACEMENT);
		gStopName = FIX_DRIVE.matcher(gStopName).replaceAll(FIX_DRIVE_REPLACEMENT);
		gStopName = FIX_STREET.matcher(gStopName).replaceAll(FIX_STREET_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = POINT.matcher(gStopName).replaceAll(POINT_REPLACEMENT);
		gStopName = POINTS.matcher(gStopName).replaceAll(POINTS_REPLACEMENT);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = ENDS_WITH.matcher(gStopName).replaceAll(EMPTY);
		return CleanUtils.cleanLabel(gStopName);
	}

	private static final String ZERO_0 = "0";

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (stopCode.length() == 0 || ZERO_0.equals(stopCode)) {
			//noinspection deprecation
			stopCode = gStop.getStopId();
		}
		stopCode = STARTS_WITH_WE_A00_.matcher(stopCode).replaceAll(EMPTY);
		return stopCode;
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		String stopId = gStop.getStopId();
		stopId = STARTS_WITH_WE_A00_.matcher(stopId).replaceAll(EMPTY);
		if (stopId.isEmpty()) {
			throw new MTLog.Fatal("Unexpected stop ID (%d) %s!", stopId, gStop.toStringPlus());
		}
		if (CharUtils.isDigitsOnly(stopId)) {
			return Integer.parseInt(stopId);
		}
		throw new MTLog.Fatal("Unexpected stop ID %s!", gStop.toStringPlus());
	}
}
