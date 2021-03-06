/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.common.Pair;

public class IsochroneUtility {
	public static List<IsochronesIntersection> computeIntersections(IsochroneMapCollection isochroneMaps)
	{
		if (isochroneMaps.size() == 1)
			return null;

		List<Integer> processedPairs = new ArrayList<Integer>();

		List<IsochronesIntersection> result = null;

		int im = 0;
		for (IsochroneMap isoMap : isochroneMaps.getIsochroneMaps())
		{
			int ii = 0;
			for (Isochrone isoLine : isoMap.getIsochrones()) 
			{
				List<IsochronesIntersection> isoIntersection = computeIntersection(isoLine, ii, isoMap, im, isochroneMaps, processedPairs);
				if (isoIntersection != null)
				{
					if (result == null)
						result = new ArrayList<IsochronesIntersection>();

					result.addAll(isoIntersection);
				}

				ii++;
			}

			im++;
		}

		// Find intersections  between IsochronesIntersection objects
		if (result != null && result.size() > 1)
		{
			List<IsochronesIntersection> isoIntersections = null;
			
			int i = 0;
			for (IsochronesIntersection isoIntersection : result)
			{
				List<IsochronesIntersection> overlaps = computeIntersection(isoIntersection, i, result);
				if (overlaps != null)
				{
					isoIntersections = new ArrayList<IsochronesIntersection>();
					isoIntersections.addAll(overlaps);
				}
                i++;
			}
			
			if (isoIntersections != null)
				result.addAll(isoIntersections);
		}

		return result;
	}

	private static List<IsochronesIntersection> computeIntersection(IsochronesIntersection isoIntersection, Integer intersectionIndex, List<IsochronesIntersection> intersections)
	{
		List<IsochronesIntersection> result = null;
		for  (int i = intersectionIndex + 1 ; i < intersections.size(); i++)
		{
			IsochronesIntersection isoIntersection2 = intersections.get(i);

			if (isoIntersection.intersects(isoIntersection2))
			{
				Geometry geomIntersection = isoIntersection.getGeometry().intersection(isoIntersection2.getGeometry());
				if (geomIntersection != null)
				{
					if (result == null)
						result = new ArrayList<IsochronesIntersection>();
					
					IsochronesIntersection isoIntersectionNew = new IsochronesIntersection(geomIntersection);
					isoIntersectionNew.addContourRefs(isoIntersection.getContourRefs());
					isoIntersectionNew.addContourRefs(isoIntersection2.getContourRefs());
				}
			}
		}

		return result;
	}

	private static List<IsochronesIntersection> computeIntersection(Isochrone isoLine, Integer isoIndex, IsochroneMap isoMap, Integer isoMapIndex, IsochroneMapCollection isochroneMaps, List<Integer> processedPairs)
	{
		List<IsochronesIntersection> result = null;
		Envelope isoEnvelope = isoLine.getEnvelope();
		Geometry isoGeometry = isoLine.getGeometry();

		for (int im = isoMapIndex + 1; im < isochroneMaps.size(); im++)
		{
			IsochroneMap isoMap2 =  isochroneMaps.getIsochrone(im);
			if (!Objects.equals(isoMap2, isoMap) && isoMap2.getEnvelope().intersects(isoEnvelope))
			{
				int ii = 0;
				for (Isochrone isoLine2 : isoMap2.getIsochrones()) 
				{
					if (isoEnvelope.intersects(isoLine2.getEnvelope()))
					{
						Geometry geomIntersection =  isoGeometry.intersection(isoLine2.getGeometry());

						if (geomIntersection != null && geomIntersection.isEmpty() == false)
						{
							if (result == null)
								result = new ArrayList<IsochronesIntersection>();

							IsochronesIntersection isoIntersection = new IsochronesIntersection(geomIntersection);
							isoIntersection.addContourRefs(new Pair<Integer, Integer>(isoMapIndex, isoIndex));
							isoIntersection.addContourRefs(new Pair<Integer, Integer>(im, ii));

							result.add(isoIntersection);
						}
					}

					ii++;
				}
			}
		}

		return result;
	}
}
