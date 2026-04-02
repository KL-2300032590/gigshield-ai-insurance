'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { api, SimulationRequest, SimulationResponse } from '@/lib/api';

/**
 * Available cities for simulation
 */
const cities = ['Mumbai', 'Delhi', 'Bangalore', 'Hyderabad', 'Chennai', 'Kolkata', 'Pune'];

/**
 * Event types for simulation
 */
const eventTypes = [
  { value: 'HEAVY_RAIN', label: 'Heavy Rain', icon: '🌧️' },
  { value: 'FLOOD', label: 'Flood', icon: '🌊' },
  { value: 'HIGH_AQI', label: 'High AQI', icon: '💨' },
  { value: 'EXTREME_HEAT', label: 'Extreme Heat', icon: '🔥' },
  { value: 'EXTREME_COLD', label: 'Extreme Cold', icon: '❄️' },
];

/**
 * Severity levels
 */
const severityLevels = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

/**
 * Quick simulation presets for common scenarios
 */
const presets = [
  { name: 'Mumbai Monsoon', city: 'Mumbai', eventType: 'HEAVY_RAIN', severity: 'HIGH', value: 75 },
  { name: 'Delhi Smog', city: 'Delhi', eventType: 'HIGH_AQI', severity: 'CRITICAL', value: 300 },
  { name: 'Chennai Heatwave', city: 'Chennai', eventType: 'EXTREME_HEAT', severity: 'HIGH', value: 45 },
  { name: 'Bangalore Flood', city: 'Bangalore', eventType: 'FLOOD', severity: 'MEDIUM', value: 100 },
];

/**
 * Simulation Control Page
 * Allows administrators to trigger weather event simulations
 */
export default function SimulationPage() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<SimulationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  
  const [formData, setFormData] = useState<SimulationRequest>({
    city: 'Mumbai',
    eventType: 'HEAVY_RAIN',
    severity: 'MEDIUM',
    duration: '2h',
    simulatedValue: 50,
    triggerClaims: true,
    triggeredBy: 'admin',
  });

  /**
   * Handle form input changes
   */
  const handleChange = (field: keyof SimulationRequest, value: string | number | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  /**
   * Apply a preset configuration
   */
  const applyPreset = (preset: typeof presets[0]) => {
    setFormData(prev => ({
      ...prev,
      city: preset.city,
      eventType: preset.eventType,
      severity: preset.severity,
      simulatedValue: preset.value,
    }));
  };

  /**
   * Submit simulation request
   */
  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await api.simulation.trigger(formData);
      setResult(response);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-white">Weather Simulation</h1>
        <p className="text-zinc-400 mt-1">
          Trigger weather events to simulate the claims workflow
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Simulation Form */}
        <div className="lg:col-span-2">
          <Card className="bg-zinc-900 border-zinc-800">
            <CardHeader>
              <CardTitle className="text-white">Simulation Parameters</CardTitle>
              <CardDescription className="text-zinc-400">
                Configure the weather event to simulate
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Quick Presets */}
              <div>
                <Label className="text-zinc-300 mb-2 block">Quick Presets</Label>
                <div className="flex flex-wrap gap-2">
                  {presets.map((preset) => (
                    <Button
                      key={preset.name}
                      variant="outline"
                      size="sm"
                      onClick={() => applyPreset(preset)}
                      className="bg-zinc-800 border-zinc-700 text-zinc-300 hover:bg-zinc-700"
                    >
                      {preset.name}
                    </Button>
                  ))}
                </div>
              </div>

              {/* City Selection */}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label className="text-zinc-300">City</Label>
                  <Select value={formData.city} onValueChange={(v) => v && handleChange('city', v)}>
                    <SelectTrigger className="bg-zinc-800 border-zinc-700 text-white">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-zinc-800 border-zinc-700">
                      {cities.map((city) => (
                        <SelectItem key={city} value={city} className="text-white">
                          {city}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label className="text-zinc-300">Event Type</Label>
                  <Select value={formData.eventType} onValueChange={(v) => v && handleChange('eventType', v)}>
                    <SelectTrigger className="bg-zinc-800 border-zinc-700 text-white">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-zinc-800 border-zinc-700">
                      {eventTypes.map((type) => (
                        <SelectItem key={type.value} value={type.value} className="text-white">
                          {type.icon} {type.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Severity and Value */}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label className="text-zinc-300">Severity</Label>
                  <Select value={formData.severity} onValueChange={(v) => v && handleChange('severity', v)}>
                    <SelectTrigger className="bg-zinc-800 border-zinc-700 text-white">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-zinc-800 border-zinc-700">
                      {severityLevels.map((level) => (
                        <SelectItem key={level} value={level} className="text-white">
                          {level}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label className="text-zinc-300">Simulated Value</Label>
                  <Input
                    type="number"
                    value={formData.simulatedValue}
                    onChange={(e) => handleChange('simulatedValue', parseFloat(e.target.value))}
                    className="bg-zinc-800 border-zinc-700 text-white"
                    placeholder="e.g., 75 for rainfall in mm"
                  />
                </div>
              </div>

              {/* Duration */}
              <div className="space-y-2">
                <Label className="text-zinc-300">Duration</Label>
                <Input
                  value={formData.duration}
                  onChange={(e) => handleChange('duration', e.target.value)}
                  className="bg-zinc-800 border-zinc-700 text-white"
                  placeholder="e.g., 2h, 30m, 1d"
                />
              </div>

              {/* Submit Button */}
              <Button
                onClick={handleSubmit}
                disabled={loading}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white"
              >
                {loading ? 'Triggering Simulation...' : '🚀 Trigger Simulation'}
              </Button>

              {/* Error Message */}
              {error && (
                <div className="p-4 bg-red-500/20 border border-red-500/30 rounded-lg">
                  <p className="text-red-400">{error}</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Result Panel */}
        <div>
          <Card className="bg-zinc-900 border-zinc-800">
            <CardHeader>
              <CardTitle className="text-white">Simulation Result</CardTitle>
            </CardHeader>
            <CardContent>
              {result ? (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-zinc-400">Status</span>
                    <Badge className={
                      result.status === 'COMPLETED' 
                        ? 'bg-green-500/20 text-green-400' 
                        : 'bg-red-500/20 text-red-400'
                    }>
                      {result.status}
                    </Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-zinc-400">Simulation ID</span>
                    <span className="text-white font-mono text-sm">{result.simulationId}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-zinc-400">City</span>
                    <span className="text-white">{result.city}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-zinc-400">Event Type</span>
                    <span className="text-white">{result.eventType}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-zinc-400">Events Published</span>
                    <span className="text-blue-400">{result.eventsPublished}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-zinc-400">Claims Triggered</span>
                    <span className="text-green-400">{result.claimsTriggered}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-zinc-400">Execution Time</span>
                    <span className="text-zinc-300">{result.executionTime}</span>
                  </div>
                  {result.message && (
                    <div className="pt-4 border-t border-zinc-800">
                      <p className="text-zinc-300 text-sm">{result.message}</p>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-8">
                  <p className="text-zinc-500">No simulation triggered yet</p>
                  <p className="text-zinc-600 text-sm mt-2">
                    Configure parameters and click trigger
                  </p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Simulation Tips */}
          <Card className="bg-zinc-900 border-zinc-800 mt-4">
            <CardHeader>
              <CardTitle className="text-white text-sm">💡 Tips</CardTitle>
            </CardHeader>
            <CardContent className="text-zinc-400 text-sm space-y-2">
              <p>• Higher severity = more claims triggered</p>
              <p>• AQI values above 200 trigger claims</p>
              <p>• Rainfall above 50mm triggers claims</p>
              <p>• Check Events page for real-time flow</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
