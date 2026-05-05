import pandas as pd
import copy as cp
import os
import csv
import json
from typing import Dict, List

def swap_file_csv(file_path: str, file_name_without_extension: str, swap_fields: list):
    # Define the file paths
    script_directory = os.path.dirname(os.path.abspath(__file__))

    # Change the working directory to the script's directory
    os.chdir(script_directory)

    EN_file_full_name = f"{file_name_without_extension}_EN.csv"
    CN_file_full_name = f"{file_name_without_extension}_CN.csv"


    # check does file_path containing file_name_without_extension, it should have
    # if it does not, meaning the file_name_without_extension is incorrectly input
    file_path = os.path.join(file_path)
    file_path_EN = file_path
    file_path_CN = file_path
    if file_name_without_extension in file_path:
        file_path_EN = os.path.join(file_path.replace(file_name_without_extension, f"{file_name_without_extension}_EN"))
        file_path_CN = os.path.join(file_path.replace(file_name_without_extension, f"{file_name_without_extension}_CN"))
    assert file_path != file_path_EN, "Check file name and file path input"
    
    # Read CSV files into dictionaries
    diction_rows_now: [List[Dict[str, str]]] = []
    diction_rows_other: [List[Dict[str, str]]] = []
    # by default is CN_to_EN, load _EN, save old data to _CN
    EN_to_CN = False

    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            print(f'Swap Load {file_name_without_extension}')
            csv_reader = csv.DictReader(file)    
            # row is List[Dict[str, str]]
            for row in csv_reader:
                diction_rows_now.append(row)  
        with open(file_path_EN, 'r', encoding='utf-8') as file:
            # by default read _EN path
            csv_reader = csv.DictReader(file)    
            for row in csv_reader:
                diction_rows_other.append(row)             
    except FileNotFoundError as e:
        # if fail to read _EN path, go into EN_to_CN mode, load _CN, save old data to _EN
        try:
            if EN_file_full_name in str(e):
                EN_to_CN = True
                with open(file_path_CN, 'r', encoding='utf-8') as file:
                    csv_reader = csv.DictReader(file)    
                    for row in csv_reader:
                        diction_rows_other.append(row)  
        except Exception as e:
            print('Failed to load both EN/CN csv')
            return

    # find common ids  between two diction list
    common_ids = set(row['id'] for row in diction_rows_now).intersection(row['id'] for row in diction_rows_other)

    for common_id in [id for id in common_ids if id and not id.startswith('#')]:
        # find a row pair with the common 'id' from both dataframes
        # ignore padding rows with empty id
        # ignore comment rows start with "#"
        row_same_id = [row for row in diction_rows_now if row['id'] == common_id ]
        row_other_same_id = [row for row in diction_rows_other if row['id'] == common_id]

        # check and swap every specified fields 1 by 1
        for field in swap_fields:
            try:
                # Extract the values from the cell
                value_now = row_same_id[0][field]
                value_other = row_other_same_id[0][field]

                # Parse JSON if the value is not empty and can be loaded as JSON
                # both values contains '{' and '}'
                if (value_now and value_other 
                    and value_now.startswith("{") and value_now.endswith("}") 
                    and value_other.startswith("{") and value_other.endswith("}") ):
                    value_now = json.loads(value_now)
                    value_other = json.loads(value_other)

                # Swap value for this field between this row pair
                row_same_id[0][field] = value_other
                row_other_same_id[0][field] = value_now

            except Exception as e:
                print(f"Error swapping values for id {common_id} and field {field}: {e}")
                continue
        
    # Save the swaped diction_rows_now to the original CSV file
    with open(file_path, 'w', encoding='utf-8', newline='') as file:
        try:
            fieldnames = diction_rows_now[0].keys()
            csv_writer = csv.DictWriter(file, fieldnames=fieldnames)
            csv_writer.writeheader()

            for row in diction_rows_now:
                try:
                    csv_writer.writerow(row)
                except Exception as e:
                    print(f"Error write row{row} csv_to_now")
                    continue
        except Exception as e:
            print("Fail to Write now csv")      
            return
         
    # Save the swaped diction_rows_other to a new CSV file
    # EN_to_CN means load _CN, the old data save to _EN as backup 
    # if load _CN, save old data to _EN 
    if EN_to_CN:
        with open(file_path_EN, 'w', encoding='utf-8', newline='') as file:
            try:
                fieldnames_other = diction_rows_other[0].keys()
                csv_writer_other = csv.DictWriter(file, fieldnames=fieldnames_other)
                print(f'Swap Write {file_name_without_extension}')
                csv_writer_other.writeheader()

                for row_other in diction_rows_other:
                    try:
                        csv_writer_other.writerow(row_other)
                    except Exception as e:
                        print(f"Error write row{row_other} csv_to_backup")
                        continue
            except Exception as e:
                print("Fail to Write other csv")
        os.remove(file_path_CN)
    else:
        with open(file_path_CN, 'w', encoding='utf-8', newline='') as file:
            try:
                fieldnames_other = diction_rows_other[0].keys()
                csv_writer_other = csv.DictWriter(file, fieldnames=fieldnames_other)
                csv_writer_other.writeheader()
                print(f'Swap Write {file_name_without_extension}')

                for row_other in diction_rows_other:
                    try:
                        csv_writer_other.writerow(row_other)
                    except Exception as e:
                        print(f"Error write row{row_other} csv_to_backup")
                        continue
            except Exception as e:
                print("Fail to Write other csv")
        os.remove(file_path_EN)
    print(f'Swap Done {file_name_without_extension}')

def swap_json(file_path: str, file_name_without_extension: str, extension: str = None):
    def read_json_with_comments(file_path):
        with open(file_path, 'r', encoding='utf-8') as file:
            lines = file.readlines()
        clean_lines = [line for line in lines if not line.strip().startswith('#')]
        return json.loads(''.join(clean_lines))

    script_directory = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_directory)

    EN_file_full_name = f"{file_name_without_extension}_EN.json"
    CN_file_full_name = f"{file_name_without_extension}_CN.json"
    if(extension):
        EN_file_full_name = f"{file_name_without_extension}_EN.{extension}"
        CN_file_full_name = f"{file_name_without_extension}_CN.{extension}"

    file_path = os.path.join(file_path)
    file_path_EN = file_path
    file_path_CN = file_path
    if file_name_without_extension in file_path:
        file_path_EN = os.path.join(file_path.replace(file_name_without_extension, f"{file_name_without_extension}_EN"))
        file_path_CN = os.path.join(file_path.replace(file_name_without_extension, f"{file_name_without_extension}_CN"))

    EN_to_CN = False
    data1 = None
    data2 = None
    try:
        data1 = read_json_with_comments(file_path)
        data2 = read_json_with_comments(file_path_EN)            
    except FileNotFoundError as e:
        if EN_file_full_name in str(e):
            EN_to_CN = True
            data2 = read_json_with_comments(file_path_CN)
        else:
            print('Failed to load both EN/CN json')
            return

    def swap_nested_json_values(data1, data2):
        for key in data2:
            # If the value is a nested dictionary, call this function recursively
            if isinstance(data2[key], dict):
                swap_nested_json_values(data1[key], data2[key])
            # Swap values for non-dictionary items
            else:
                temp = data2[key]
                data2[key] = data1[key]
                data1[key] = temp
    swap_nested_json_values(data1, data2)

    with open(file_path, 'w', encoding='utf-8') as output:
        json.dump(data1, output, ensure_ascii=False, indent=2)

    if EN_to_CN:
        with open(file_path_EN, 'w', encoding='utf-8') as output:
            json.dump(data2, output, ensure_ascii=False, indent=2)
        print(f'Swap Done {file_name_without_extension}')
        os.remove(file_path_CN)
    else:
        with open(file_path_CN, 'w', encoding='utf-8') as output:
            json.dump(data2, output, ensure_ascii=False, indent=2)  
        print(f'Swap Done {file_name_without_extension}')
        os.remove(file_path_EN)
 
def swap_name(file_path: str, file_name_with_ext: str):
    # Split the file name and extension
    base_name, extension = os.path.splitext(file_name_with_ext)
    
    # Form the names for both "_CN.txt" and "_EN.txt"
    cn_file_name = f"{base_name}_CN{extension}"
    en_file_name = f"{base_name}_EN{extension}"
    
    # Get the directory of the file
    file_directory = os.path.dirname(file_path)
    
    # Check if the files exist in the directory
    cn_file_path = os.path.join(file_directory, cn_file_name)
    en_file_path = os.path.join(file_directory, en_file_name)
    
    if os.path.exists(cn_file_path):
        # Swap names
        os.rename(file_path, en_file_path)
        os.rename(cn_file_path, file_path)
        print(f"Swapped names: {file_name_with_ext} <-> {cn_file_name}")
    elif os.path.exists(en_file_path):
        # Swap names
        os.rename(file_path, cn_file_path)
        os.rename(en_file_path, file_path)
        print(f"Swapped names: {file_name_with_ext} <-> {en_file_name}")
    else:
        print(f"No corresponding {file_name_with_ext} found.")

def update_setting_in_json(file_path, key, new_value):
    try:
        # Open the settings file in read mode to load existing data
        with open(file_path, 'r',encoding='utf-8') as file:
            settings = json.load(file)
        
        # Check if the key exists in the JSON, if so, update the value
        if key in settings:
            if new_value is None:
                settings[key] = not settings[key] 
            else:
                settings[key] = new_value

            # Open the file in write mode to update the file with new data
            with open(file_path, 'w', encoding='utf-8') as file:
                json.dump(settings, file, indent=2, ensure_ascii=False)
            print(f"Value of '{key} in {file_path}' has been updated to {settings[key]}.")
        else:
            print(f"Key '{key}' not found in the settings file.")
    
    except FileNotFoundError:
        print(f"File '{file_path}' not found.")
    except json.JSONDecodeError:
        print(f"Error decoding JSON from file '{file_path}'.")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

def swap_variants(subdirectory='', folder_name='variants'):
    # Get the directory where the script is located
    script_directory = os.path.dirname(os.path.abspath(__file__))
    script_directory += f'{subdirectory}'

    # Define the paths
    variants_path = os.path.join(script_directory, f'{folder_name}')
    variants_cn_path = os.path.join(script_directory, f'{folder_name}_CN')
    variants_en_path = os.path.join(script_directory, f'{folder_name}_EN')

    # Print out the paths for debugging
    print(f"Script directory: {script_directory}")
    print(f"{folder_name} path: {variants_path}")
    print(f"{folder_name}_cn path: {variants_cn_path}")
    print(f"{folder_name}_en path: {variants_en_path}")
    
    # Check existence of directories
    print(f'Does "{folder_name}" exist? {"Yes" if os.path.exists(variants_path) else "No"}')
    print(f'Does "{folder_name}_cn" exist? {"Yes" if os.path.exists(variants_cn_path) else "No"}')
    print(f'Does "{folder_name}_en" exist? {'Yes' if os.path.exists(variants_en_path) else "No"}')

    # Check if the current backup is "variants_cn"
    if os.path.exists(variants_cn_path) and not os.path.exists(variants_en_path):
        # Rename "variants" to "variants_en"
        os.rename(variants_path, variants_en_path)
        # Rename "variants_cn" to "variants"
        os.rename(variants_cn_path, variants_path)
        print(f'Swapped "{folder_name}_cn" with "{folder_name}".')

    # Check if the current backup is "variants_en"
    elif os.path.exists(variants_en_path) and not os.path.exists(variants_cn_path):
        # Rename "variants" to "variants_cn"
        os.rename(variants_path, variants_cn_path)
        # Rename "variants_en" to "variants"
        os.rename(variants_en_path, variants_path)
        print(f'Swapped "{folder_name}_en" with "{folder_name}".')

    else:
        print('No swap performed. Please ensure the directories exist and try again.')
     
if __name__ == "__main__":
    swap_file_csv("data/campaign/industries.csv", "industries", ['desc'])
    swap_file_csv("data/campaign/market_conditions.csv", "market_conditions", ['desc'])
    swap_file_csv("data/campaign/rules.csv", "rules", ['script','text','options'])
    swap_file_csv("data/campaign/special_items.csv", "special_items", ['name','desc'])
    swap_file_csv("data/characters/skills/skill_data.csv", "skill_data", ['description'])
    swap_file_csv("data/hullmods/hull_mods.csv","hull_mods",['name','tech/manufacturer','uiTags','desc','short','sModDesc'])
    swap_file_csv("data/hulls/ship_data.csv", "ship_data", ['name','tech/manufacturer','designation'])
    # swap_file_csv("data/hulls/wing_data.csv", "wing_data", ['role desc',])
    swap_name("data/hulls/wing_data.csv", "wing_data.csv")
    swap_file_csv("data/shipsystems/ship_systems.csv", "ship_systems", ['name'])
    swap_file_csv("data/strings/descriptions.csv", "descriptions", ['text1','text2','text3','text4'])
    swap_file_csv("data/weapons/weapon_data.csv","weapon_data",['name','tech/manufacturer','primaryRoleStr','speedStr','trackingStr','customPrimary','customPrimaryHL'])
    swap_name("data/campaign/HPSIDTales.json", "HPSIDTales.json")
    swap_name("data/characters/skills/HSI_Knight_SP.skill", "HSI_Knight_SP.skill")
    swap_name("data/characters/skills/HSI_Knight.skill", "HSI_Knight.skill")
    swap_name("data/config/exerelin/customStarts.json", "customStarts.json")
    swap_name("data/config/modFiles/HSIStellaArena/HSIStellaArenaadventureModeBuffs.json", "HSIStellaArenaadventureModeBuffs.json")
    swap_name("data/config/modFiles/HSIStellaArena/HSIStellaArenaAdventureModeEnemies.json", "HSIStellaArenaAdventureModeEnemies.json")
    swap_name("data/config/modFiles/HSIStellaArena/Kite_fleet.csv", "Kite_fleet.csv")
    swap_name("data/config/modFiles/HSIStellaArena/Vangaurd_fleet.csv", "Vangaurd_fleet.csv")
    swap_name("data/config/modFiles/HSIStellaArena/Wolf_fleet.csv", "Wolf_fleet.csv")
    swap_name("data/config/modFiles/magicBounty_data.json", "magicBounty_data.json")
    swap_name("data/config/custom_entities.json", "custom_entities.json")
    swap_name("data/config/planets.json", "planets.json")
    swap_name("data/config/settings.json", "settings.json")
    swap_name("data/strings/strings.json", "strings.json")
    swap_name("data/world/factions/GF3.faction", "GF3.faction")
    swap_name("data/world/factions/HSI_Stalker.faction", "HSI_Stalker.faction")
    swap_name("data/world/factions/HSI.faction", "HSI.faction")
    swap_name("mod_info.json","mod_info.json")
    swap_name("data/missions/HSI_Fuse/descriptor.json", "descriptor.json")
    swap_name("data/missions/HSI_Fuse/mission_text.txt", "mission_text.txt")
    swap_name("data/missions/HSI_MovingCitadel/descriptor.json", "descriptor.json")
    swap_name("data/missions/HSI_MovingCitadel/mission_text.txt", "mission_text.txt")
    swap_name("data/missions/HSI_Test/descriptor.json", "descriptor.json")
    swap_name("data/missions/HSI_Test/mission_text.txt", "mission_text.txt")
    swap_name("data/missions/HSI_TheFinale/descriptor.json", "descriptor.json")
    swap_name("data/missions/HSI_TheFinale/mission_text.txt", "mission_text.txt")
    swap_variants(r'\data')
    swap_variants(r'\data\hulls','skins')
