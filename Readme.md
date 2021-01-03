#### moisture protection calculation

This program was created in order to understand the Glaser-Schema as specified in DIN 4108-3.

It reads Constructions inside the Input Folder. Bauteil.json is constructed like this:  
```json
{
  "Schichten": [
    {
      "Name": "Betonrohdecke",
      "Dicke": 0.245,
      "Waermeleitfaehigkeit": 1.550,
      "Diffusionswiderstand": 53.0
    },
    {
      "Name": "Gefaellebeton",
      "Dicke": 0.060,
      "Waermeleitfaehigkeit": 1.400,
      "Diffusionswiderstand": 20.0
    },
    {
      "Name": "Dampfbremse",
      "Dicke": 0.002,
      "Waermeleitfaehigkeit": 0.175,
      "Diffusionswiderstand": 22500.0
    },
    {
      "Name": "Daemmstoff",
      "Dicke": 0.12,
      "Waermeleitfaehigkeit": 0.045,
      "Diffusionswiderstand": 45.0
    },
    {
      "Name": "Dichtungsbahn",
      "Dicke": 0.005,
      "Waermeleitfaehigkeit": 0.175,
      "Diffusionswiderstand": 27000.0
    },
    {
      "Name": "Kies",
      "Dicke": 0.010,
      "Waermeleitfaehigkeit": 0.950,
      "Diffusionswiderstand": 7.0
    }
  ]
}
```  

The singular Objects inside the Array describe the layers of construction **in order** from the interior surface to the exterior surface.  
Each Layer is described by its properties (in German).
```json
{
  "Name": "Betonrohdecke",
  "Dicke": 0.245,
  "Waermeleitfaehigkeit": 1.550,
  "Diffusionswiderstand": 53.0
}
```
| Name              | Dicke                  | Waermeleitfaehigkeit   | Diffusionswiderstand     |
| :-------------    | :-------------         | :-------------------   | :-------------------     |
| Name of the layer | Thickness of the layer | Thermal Conductivity λ | Water Vaper Resistance μ |

It will then output a *.txt* file inside the Output folder with tables containing the thermal profile, the pressure profile and the condensation mass and the evaporating mass.  

The table will be a ASCII table.  

-----------------------

#### Todos

 - [ ] Adding the case of two seperate k-layers.
 - [ ] Really reading the Input directory.
 - [ ] Specifying the boundary conditions.
 - [ ] Writing documentation
