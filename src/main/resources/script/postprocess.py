import geopandas as gp
import json
import sys

if __name__ == '__main__':
    plan = {}

    precinct_data = gp.read_file(sys.argv[1])
    precinct_data = precinct_data[['ID', 'geometry']]
    precinct_data['district_id'] = 0

    f = open(sys.argv[2])
    result = json.load(f)
    
    for i in range(len(result['districtingPlans'])):
        sub_result = result['districtingPlans'][i]
        index = 0
        precinct_data_c = precinct_data
        for district in sub_result['districts']:
            for precinct in district:
                precinct_data_c.at[precinct, 'district_id'] = index
            index += 1
        precinct_data_c['geometry'] = precinct_data_c.buffer(0)
        precinct_data_c = precinct_data_c.dissolve(by='district_id')
        key = 'plan' + str(i+1)
        data = json.loads(precinct_data_c.to_json())
        plan[key] = data
    f.close()
    with open("src/main/resources/districting/districtings.json", "w") as outfile:
        json.dump(plan, outfile)
